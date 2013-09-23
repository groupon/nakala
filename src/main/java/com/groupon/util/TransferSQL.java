/*
Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.groupon.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class TransferSQL {

    private static final Logger log = Logger.getLogger(TransferSQL.class);

    private Connection fSrc;
    private Connection fDst;
    private String fSrcTable;
    private String fDstTable;
    private String fSrcWhere;
    private int fnBatchSize = 100;

    public TransferSQL() {
    }

    public Connection getSrc() {
        return fSrc;
    }

    public void setSrc(Connection src) {
        fSrc = src;
    }

    public Connection getDst() {
        return fDst;
    }

    public void setDst(Connection dst) {
        fDst = dst;
    }

    public String getSrcTable() {
        return fSrcTable;
    }

    public void setSrcTable(String srcTable) {
        fSrcTable = srcTable;
    }

    public String getDstTable() {
        return fDstTable;
    }

    public void setDstTable(String dstTable) {
        fDstTable = dstTable;
    }

    public String getSrcWhere() {
        return fSrcWhere;
    }

    public void setSrcWhere(String srcWhere) {
        fSrcWhere = srcWhere;
    }

    public int getBatchSize() {
        return fnBatchSize;
    }

    public void setBatchSize(int batchSize) {
        fnBatchSize = batchSize;
    }

    private ResultSet executeQuery(Connection c, String what) throws SQLException {
        String who = c == fSrc ? "SRC: " : "DST: ";
        log.debug(who + ": " + what);
        return c.createStatement().executeQuery(what);
    }

    private void execute(Connection c, String what) throws SQLException {
        String who = c == fSrc ? "SRC: " : "DST: ";
        log.debug(who + ": " + what);
        c.createStatement().execute(what);
    }

    private String getCreateTable(Connection c, String srcTable, String dstTable) throws Exception {
        String show = "SHOW CREATE TABLE " + srcTable;
        ResultSet rs = executeQuery(c, show);
        if (!rs.next())
            throw new Exception("No rows returned for " + show);
        String ddl = rs.getString(2);
        ddl = ddl.replaceFirst("`" + srcTable + "`", "`" + dstTable + "`");
        rs.close();
        return ddl;
    }

    private ResultSet readTable(Connection c, String table, String where) throws SQLException {
        Statement st = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        st.setFetchSize(Integer.MIN_VALUE);
        String query = "SELECT * FROM " + table;
        if ((where != null) && (where.trim().length() > 0)) {
            query = query + " WHERE " + where;
        }
        log.debug("SRC: " + query);
        return st.executeQuery(query);
    }

    private void insertBatch(PreparedStatement st, int batch) throws SQLException {
        log.debug("Inserting batch size => " + batch);
        st.executeBatch();
    }

    private PreparedStatement prepareInsert(Connection c, ResultSet rs, String table) throws SQLException {
        StringBuilder insSql = new StringBuilder();
        int nCols = rs.getMetaData().getColumnCount();
        insSql.append("INSERT INTO " + table + " VALUES (");
        String sep = "";
        for (int iCol = 0; iCol < nCols; iCol++) {
            insSql.append(sep).append("?");
            sep = ",";
        }
        insSql.append(")");
        return fDst.prepareStatement(insSql.toString());
    }

    public void transfer() throws Exception {
        String ddl = getCreateTable(fSrc, fSrcTable, fDstTable);

        execute(fDst, "DROP TABLE IF EXISTS " + fDstTable);
        execute(fDst, ddl);

        execute(fDst, "SET NAMES latin1");
        execute(fSrc, "SET NAMES latin1");

        ResultSet sel = readTable(fSrc, fSrcTable, fSrcWhere);
        PreparedStatement ins = prepareInsert(fDst, sel, fDstTable);
        int nCols = sel.getMetaData().getColumnCount();
        int iBatch = 0;
        for (; sel.next(); ) {
            for (int iCol = 1; iCol <= nCols; iCol++) {
                ins.setObject(iCol, sel.getObject(iCol));
            }
            ins.addBatch();
            if (++iBatch >= fnBatchSize) {
                insertBatch(ins, iBatch);
            }
        }
        if (iBatch > 0) {
            insertBatch(ins, iBatch);
        }
    }

    public static <W extends Writer> W usage(W w, String... msgs) {
        PrintWriter pw = new PrintWriter(w);
        pw.println("Usage: " + TransferSQL.class.getName() + " options... ");
        pw.println();
        pw.println("-s user/pass/database@source-jdbc-url");
        pw.println("    Specify the connection information for the source database.");
        pw.println("-d user/pass/database@destination-jdbc-url");
        pw.println("    Specify the connection information for the destination database.");
        pw.println("-t table");
        pw.println("    Name the source table to be copied.");
        pw.println("-T table");
        pw.println("    Set the name of the table in the destination.");
        pw.println("-w where");
        pw.println("    Filter the contents of the source table when transferring.");
        pw.println("-b batch-size");
        pw.println("    Batch insert size to use.");
        if (msgs.length > 0) {
            for (String msg : msgs) {
                pw.println(msg);
            }
        }
        return w;
    }

    public static <O extends OutputStream> O usage(O out, String... msgs) throws IOException {
        usage(new OutputStreamWriter(out), msgs).flush();
        return out;
    }

    private static Connection parseConnection(String arg) throws SQLException {
        String[] bits = arg.split("/", 3);
        String user = bits[0];
        String pass = bits[1];
        String url = bits[2];

        return DriverManager.getConnection(url, user, pass);
    }

    public static void main(String[] args) throws Exception {
        TransferSQL t = new TransferSQL();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("-s")) {
                t.setSrc(parseConnection(args[++i]));
            } else if (a.equals("-d")) {
                t.setDst(parseConnection(args[++i]));
            } else if (a.equals("-w")) {
                t.setSrcWhere(args[++i]);
            } else if (a.equals("-t")) {
                t.setSrcTable(args[++i]);
            } else if (a.equals("-T")) {
                t.setDstTable(args[++i]);
            } else if (a.equals("-b")) {
                t.setBatchSize(Integer.parseInt(args[++i]));
            } else {
                usage(System.err, "Unrecognized argument: " + a);
                System.exit(1);
            }
        }
        List<String> errs = new LinkedList<String>();
        if (t.getDst() == null) {
            if (t.getSrc() != null) {
                t.setDst(t.getSrc());
            } else {
                errs.add("Please specify a destination connection.");
            }
        }
        if (t.getSrc() == null) {
            if (t.getDst() != null) {
                t.setSrc(t.getDst());
            } else {
                errs.add("Please specify a source connection.");
            }
        }
        if (t.getSrcTable() == null) {
            errs.add("Please specify a source table.");
        }
        if (t.getDstTable() == null) {
            errs.add("Please specify a destination table.");
        }
        if (errs.size() > 0) {
            usage(System.err, errs.toArray(new String[errs.size()]));
            System.exit(1);
        }

        t.transfer();
    }
}

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

package com.groupon.util.io;

import com.groupon.util.Objects;
import com.groupon.util.collections.CollectionUtil;
import com.groupon.util.collections.MapFn;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author alasdair@groupon.com
 */
public class IoUtil {

    private static ByteArrayOutputStream gSavedStdin;
    private static boolean gSaveStdin;

    private static String gDefaultEncoding = "UTF-8";

    private static class SaveInputStream extends InputStream {
        private InputStream fIn;

        public SaveInputStream(InputStream in) {
            fIn = in;
            gSavedStdin = new ByteArrayOutputStream();
        }

        @Override
        public void close() throws IOException {
            fIn.close();
        }

        @Override
        public int read() throws IOException {
            int c = fIn.read();
            if (c >= 0)
                gSavedStdin.write(c);
            return c;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = fIn.read(b, off, len);
            if (n > 0)
                gSavedStdin.write(b, off, len);
            return n;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int n = fIn.read(b);
            if (n > 0)
                gSavedStdin.write(b);
            return n;
        }
    }

    public static final LineConverter<String> NO_CONVERT = new LineConverter<String>() {
        @Override
        public String convert(String line) {
            return line;
        }
    };

    public static final LineConverter<String> TO_LOWER = new LineConverter<String>() {
        @Override
        public String convert(String line) {
            return line.toLowerCase();
        }
    };

    public static final LineConverter<String> TO_UPPER = new LineConverter<String>() {
        @Override
        public String convert(String line) {
            return line.toUpperCase();
        }
    };
    public static final LineConverter<Long> TO_LONG = new LineConverter<Long>() {
        @Override
        public Long convert(String line) {
            return Long.parseLong(line);
        }
    };

    public static String getDefaultEncoding() {
        return gDefaultEncoding;
    }

    public static void setDefaultEncoding(String encoding) {
        gDefaultEncoding = encoding;
    }

    public static InputStream input(File file) throws IOException {
        return input(file.getAbsolutePath());
    }

    public static InputStream input(String file) throws IOException {
        if (file.equals("-")) {
            InputStream in;
            if (gSavedStdin != null) {
                byte[] ss = gSavedStdin.toByteArray();
                gSavedStdin = null;
                in = new MultiInputStream(new ByteArrayInputStream(ss), new DontCloseInputStream(System.in));
            } else {
                in = new DontCloseInputStream(System.in);
            }
            if (gSaveStdin)
                in = new SaveInputStream(in);
            return in;
        }
        InputStream in;
        if (file.startsWith("!")) {
            in = IoUtil.class.getResourceAsStream("/" + file.substring(1).trim());
        } else {
            in = new FileInputStream(file);
        }
        if (file.endsWith(".gz"))
            return new GZIPInputStream(in);
        return in;
    }

    public static InputStream input(Class<?> cls, String resource) throws IOException {
        InputStream in = cls.getResourceAsStream(resource);
        if (in != null && resource.endsWith(".gz")) {
            in = new GZIPInputStream(in);
        }
        return in;
    }

    public static OutputStream output(String file) throws IOException {
        return output(file, false);
    }

    public static OutputStream output(String file, boolean bAppend) throws IOException {
        if (file.equals("-"))
            return new DontCloseOutputStream(System.out);
        File f = new File(file);
        if (f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        OutputStream out = new FileOutputStream(file, bAppend);
        if (file.endsWith(".gz"))
            return new GZIPOutputStream(out);
        return out;
    }

    public static OutputStream output(File file) throws IOException {
        return output(file.getAbsolutePath());
    }

    public static Reader read(File f) throws IOException {
        return read(f, gDefaultEncoding);
    }

    public static Reader read(String file) throws IOException {
        return read(file, gDefaultEncoding);
    }

    public static Reader read(Class<?> cls, String resource) throws IOException {
        return read(cls, resource, gDefaultEncoding);
    }

    public static Reader read(File f, String encoding) throws IOException {
        return read(f.getAbsolutePath(), encoding != null ? encoding : gDefaultEncoding);
    }

    public static Reader read(Class<?> cls, String resource, String encoding) throws IOException {
        InputStream in = cls.getResourceAsStream(resource);
        if (in == null)
            return null;
        if (resource.endsWith(".gz"))
            in = new GZIPInputStream(in);
        return new InputStreamReader(in, encoding != null ? encoding : gDefaultEncoding);
    }

    public static Reader read(String file, String encoding) throws IOException {
        return new InputStreamReader(input(file), encoding != null ? encoding : gDefaultEncoding);
    }

    public static File getFile(Class<?> cls, String resource) throws IOException {
        URL url = cls.getResource(resource);
        if (url == null)
            return null;
        return new File(url.getFile());
    }

    public static Writer write(String file) throws IOException {
        return write(file, gDefaultEncoding);
    }

    public static Writer write(String file, String encoding) throws IOException {
        return new OutputStreamWriter(output(file), encoding != null ? encoding : gDefaultEncoding);
    }

    public static Writer write(File file) throws IOException {
        return write(file, gDefaultEncoding);
    }

    public static Writer write(File file, String encoding) throws IOException {
        return write(file.getAbsolutePath(), encoding);
    }

    public static Writer append(String file) throws IOException {
        return append(file, gDefaultEncoding);
    }

    public static Writer append(String file, String encoding) throws IOException {
        return new OutputStreamWriter(output(file, true), encoding != null ? encoding : gDefaultEncoding);
    }

    public static Writer append(File file) throws IOException {
        return append(file, gDefaultEncoding);
    }

    public static Writer append(File file, String encoding) throws IOException {
        return append(file.getAbsolutePath(), encoding);
    }

    public static <W extends Writer> W copy(Reader r, W w) throws IOException {
        char[] buf = new char[8192];
        for (int n; (n = r.read(buf, 0, buf.length)) >= 0; )
            w.write(buf, 0, n);
        r.close();
        return w;
    }

    public static <W extends Writer> W copy(Reader r, W w, LineEditor sed) throws IOException {
        if (sed == null)
            return copy(r, w);

        for (String line : readLines(r)) {
            w.write(sed.edit(line));
            w.write('\n');
        }
        return w;
    }

    public static <O extends OutputStream> O copy(InputStream in, O out) throws IOException {
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf, 0, buf.length)) >= 0; )
            out.write(buf, 0, n);
        in.close();
        return out;
    }

    public static void saveStdin() {
        gSaveStdin = true;
    }

    public static void releaseStdin() {
        gSaveStdin = false;
    }

    public static boolean delete(File f) {
        if (f == null)
            return false;
        boolean b = true;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) {
                for (File kid : kids) {
                    b = delete(kid) && b;
                }
            }
        }
        return f.delete() && b;
    }

    public static void print(PrintWriter w, String sep, Object... vals) {
        if (w == null)
            return;

        String s = "";
        for (Object val : Objects.varargs(vals)) {
            w.print(s);
            w.print(String.valueOf(val));
            s = sep;
        }
    }

    public static void println(PrintWriter w, String sep, Object... vals) {
        if (w == null)
            return;
        print(w, sep, vals);
        w.println();
    }

    public static <W extends Writer> W write(W w, Object... vals) throws IOException {
        for (Object val : Objects.varargs(vals)) {
            w.write(String.valueOf(val));
        }
        return w;
    }

    public static <O extends OutputStream> O write(O o, Object... vals) throws IOException {
        for (Object val : Objects.varargs(vals)) {
            o.write(String.valueOf(val).getBytes());
        }
        return o;
    }

    public static Iterable<String> readLines(String file) throws IOException {
        return readLines(IoUtil.read(file));
    }

    public static Iterable<String> readLines(final Reader r) throws IOException {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new LineIterator<String>(r, NO_CONVERT);
            }
        };
    }

    public static <T extends Object> Iterable<T> readLines(Reader r, MapFn<String, T> map) throws IOException {
        return CollectionUtil.iterable(readLines(r), map);
    }

    public static <T, C extends Collection<T>> C readCollection(Reader r, C c, LineConverter<T> cvt) throws IOException {
        for (String line : readLines(r)) {
            T obj = cvt.convert(line);
            if (obj != null) {
                c.add(obj);
            }
        }
        return c;
    }

    public static <C extends Collection<String>> C readCollection(Reader r, C c) throws IOException {
        for (String line : readLines(r)) {
            c.add(line);
        }
        return c;
    }

    public static int countLines(Reader r) throws IOException {
        int nLines = 0;
        for (@SuppressWarnings("unused") String line : IoUtil.readLines(r))
            nLines++;
        return nLines;
    }

    public static <T extends Object> Iterator<T> iterate(final Iterator<String> it, final LineConverter<T> cvt) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return cvt.convert(it.next());
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    public static BufferedReader skipLines(Reader read, int n) throws IOException {
        BufferedReader r = (read instanceof BufferedReader) ? (BufferedReader) read : new BufferedReader(read);
        for (int i = 0; i < n && r.readLine() != null; i++) ;
        return r;
    }

    public static void main(String[] args) throws IOException {
        for (String line : IoUtil.readLines(IoUtil.read(args.length > 0 ? args[0] : "-"))) {
            System.out.println(line);
        }
    }

    public static String file(Class<?> cls, String resource) {
        String path = cls.getResource(resource).toExternalForm();
        return path.substring(path.indexOf(":") + 1);
    }

    public static <T extends Object> Iterable<T> iterable(final String src, final MapFn<String, T> mapper) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                try {
                    return readLines(read(src), mapper).iterator();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Write contents to a temp file that is deleted on exit and return file name;
     *
     * @param contents
     * @return
     */

    public static String createTempFile(String contents) throws IOException {
        File temp = File.createTempFile("temp_contents_", ".txt");
        temp.deleteOnExit();
        Writer writer = write(temp);
        writer.write(contents);
        writer.close();
        return temp.getAbsolutePath();
    }
}

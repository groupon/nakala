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

package com.groupon.ml;

import com.groupon.nakala.core.Id;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */

public class ClassificationReportWriter {
    private static final int ROW_HEADING = 0;
    private static final int TP_COL = 1;
    private static final int FP_COL = 2;
    private static final int FN_COL = 3;
    private static final int P_COL = 4;
    private static final int R_COL = 5;
    private static final int F_COL = 6;
    private static final int ACC_COL = 1;

    public static void writeStats(Sheet sheet, ClassificationMetrics metrics) {
        int rowNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(0);
        cell.setCellValue("Score threshold");
        cell = row.createCell(1);
        cell.setCellValue(metrics.getThreshold());

        ++rowNum; // empty row after heading
        row = sheet.createRow(rowNum++);
        cell = row.createCell(ROW_HEADING);
        cell.setCellValue("Category");
        cell = row.createCell(TP_COL);
        cell.setCellValue("TP");
        cell = row.createCell(FP_COL);
        cell.setCellValue("FP");
        cell = row.createCell(FN_COL);
        cell.setCellValue("FN");
        cell = row.createCell(P_COL);
        cell.setCellValue("P");
        cell = row.createCell(R_COL);
        cell.setCellValue("R");
        cell = row.createCell(F_COL);
        cell.setCellValue("F");

        for (int i = 0; i < metrics.getCategories().size(); ++i) {
            String category = metrics.getCategories().get(i);

            row = sheet.createRow(rowNum++);

            cell = row.createCell(ROW_HEADING);
            cell.setCellValue(metrics.getCategories().get(i));
            cell = row.createCell(TP_COL);
            cell.setCellValue(metrics.getTp(category));
            cell = row.createCell(FP_COL);
            cell.setCellValue(metrics.getFp(category));
            cell = row.createCell(FN_COL);
            cell.setCellValue(metrics.getFn(category));
            cell = row.createCell(P_COL);
            cell.setCellValue(metrics.getP(category));
            cell = row.createCell(R_COL);
            cell.setCellValue(metrics.getR(category));
            cell = row.createCell(F_COL);
            cell.setCellValue(metrics.getF1(category));
        }

        // macro-averages
        ++rowNum;

        row = sheet.createRow(rowNum++);
        cell = row.createCell(ROW_HEADING);
        cell.setCellValue("Total");
        cell = row.createCell(TP_COL);
        cell.setCellValue(metrics.getTp());
        cell = row.createCell(FP_COL);
        cell.setCellValue(metrics.getFp());
        cell = row.createCell(FN_COL);
        cell.setCellValue(metrics.getFn());

        rowNum += 2;
        row = sheet.createRow(rowNum++);
        cell = row.createCell(ROW_HEADING);
        cell.setCellValue("Macro-averages");
        cell = row.createCell(P_COL);
        cell.setCellValue(metrics.getMacroAvgP());
        cell = row.createCell(R_COL);
        cell.setCellValue(metrics.getMacroAvgR());
        cell = row.createCell(R_COL);
        cell.setCellValue(metrics.getMarcoAvgF1());
        cell = row.createCell(F_COL);
        cell.setCellValue(metrics.getMarcoAvgF1());

        // micro-averages
        row = sheet.createRow(rowNum++);
        cell = row.createCell(ROW_HEADING);
        cell.setCellValue("Micro averages");
        cell = row.createCell(P_COL);
        cell.setCellValue(metrics.getMicroAvgP());
        cell = row.createCell(R_COL);
        cell.setCellValue(metrics.getMicroAvgR());
        cell = row.createCell(F_COL);
        cell.setCellValue(metrics.getMicroAvgF1());

        rowNum += 2;
        row = sheet.createRow(rowNum);
        cell = row.createCell(ROW_HEADING);
        cell.setCellValue("Average Accuracy");
        cell = row.createCell(ACC_COL);
        cell.setCellValue(metrics.getAvgAccuracy());
    }

    public static void writeDetails(Sheet sheet,
                                    final List<String> categories,
                                    final Map<Id, Set<String>> id2truecategories,
                                    final ClassificationAnalysisCollector id2predictions) {

        Row row = sheet.createRow(0);
        for (int i = 0; i < categories.size(); ++i) {
            Cell cell = row.createCell(i + 1);
            cell.setCellValue(categories.get(i));
        }
        Cell cell = row.createCell(categories.size() + 1);
        cell.setCellValue("True Categories");
        int rowNum = 0;

        for (Map.Entry<Id, Set<String>> e : id2truecategories.entrySet()) {
            row = sheet.createRow(++rowNum);

            ClassificationAnalysis predictions = id2predictions.get(e.getKey());
            cell = row.createCell(0);
            cell.setCellValue(e.getKey().toString()); //ID
            for (int i = 0; i < categories.size(); ++i) {
                cell = row.createCell(i + 1);
                cell.setCellValue(predictions.getScore(categories.get(i)));
            }
            int i = categories.size() + 1;
            for (String category : id2truecategories.get(e.getKey())) {
                cell = row.createCell(i++);
                cell.setCellValue(category);
            }
        }
    }
}

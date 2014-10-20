package com.dmitriytkachenko.nettyhttpserver;

import java.util.List;

public class HtmlCreator {
    private StringBuilder html = new StringBuilder();

    public HtmlCreator() {
        html.append("<!DOCTYPE html>");
        html.append("<head>");
        html.append("<meta charset=\"utf-8\">");
    }

    public void setTitle(String title) {
        html.append("<title>").append(title).append("</title>");
    }

    public void setH1(String heading) {
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>").append(heading).append("</h1>");
    }

    public void addParagraph(String paragraph) {
        html.append("<p>").append(paragraph).append("</p>");
    }

    public void addTableWithHeaders(List<String> headers) {
        html.append("<table>");
        html.append("<tr>");
        headers.forEach((h) -> html.append("<th>").append(h).append("</th>"));
        html.append("</tr>");
    }

    public void addRowToTable(List<String> rowElements) {
        html.append("<tr>");
        rowElements.forEach((re) -> html.append("<td>").append(re).append("</td>"));
        html.append("</tr>");
    }

    public void endTable() {
        html.append("</table>");
    }

    public String getHtml() {
        html.append("</body>");
        return html.toString();
    }
}

package com.dmitriytkachenko.nettyhttpserver;

import java.util.List;

public class HtmlCreator {
    private StringBuilder html = new StringBuilder();

    public HtmlCreator() {
        html.append("<!DOCTYPE html>");
        html.append("<head>");
        html.append("<meta charset=\"utf-8\">");
    }

    public HtmlCreator setTitle(String title) {
        html.append("<title>").append(title).append("</title>");
        return this;
    }

    public HtmlCreator setH1(String heading) {
        html.append("</head>");
        html.append("<body>");
        html.append("<h1>").append(heading).append("</h1>");
        return this;
    }

    public HtmlCreator addH2(String heading) {
        html.append("<h2>").append(heading).append("</h2>");
        return this;
    }

    public HtmlCreator addParagraph(String paragraph) {
        html.append("<p>").append(paragraph).append("</p>");
        return this;
    }

    public HtmlCreator openParagraph() {
        html.append("<p>");
        return this;
    }

    public HtmlCreator addBold(String text) {
        html.append("<b>").append(text).append("</b>");
        return this;
    }

    public HtmlCreator addText(String text) {
        html.append(text);
        return this;
    }

    public HtmlCreator closeParagraph() {
        html.append("</p>");
        return this;
    }

    public HtmlCreator addHorizontalLine() {
        html.append("<hr>");
        return this;
    }

    public HtmlCreator addTableWithHeaders(List<String> headers) {
        html.append("<table>");
        html.append("<tr>");
        headers.forEach((h) -> html.append("<th>").append(h).append("</th>"));
        html.append("</tr>");
        return this;
    }

    public HtmlCreator addRowToTable(List<String> rowElements) {
        html.append("<tr>");
        rowElements.forEach((re) -> html.append("<td>").append(re).append("</td>"));
        html.append("</tr>");
        return this;
    }

    public HtmlCreator endTable() {
        html.append("</table>");
        return this;
    }

    public HtmlCreator openStyle() {
        html.append("<style>");
        return this;
    }

    public HtmlCreator closeStyle() {
        html.append("</style>");
        return this;
    }

    public HtmlCreator centerHeadings() {
        html.append("h1, h2 { text-align: center; }");
        return this;
    }

    public HtmlCreator styleTables() {
        html.append("table, th, td { margin: 0 auto; border: 1px solid black; border-collapse: collapse; text-align: center; }");
        return this;
    }

    public String getHtml() {
        html.append("</body>");
        return html.toString();
    }
}

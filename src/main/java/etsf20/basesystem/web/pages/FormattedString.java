package etsf20.basesystem.web.pages;

import java.util.stream.Collectors;

/**
 * Html formatted string created using method chaining
 * <p>
 * <b>Example of use:</b>
 * <pre>
 * {@code
 * FormattedString s =
 *      new FormattedString()
 *          .text("Start unformatted ")
 *          .italic("and then add something emphasized ")
 *          .bold("or bold");
 *
 * System.out.println(s);
 * }
 * </pre>
 */
public class FormattedString {
    private final StringBuilder sb = new StringBuilder();

    private static String escapeHTML(String str) {
        return str.codePoints().mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ?
                        "&#" + c + ";" : new String(Character.toChars(c)))
                .collect(Collectors.joining());
    }

    /**
     * Add normal text
     */
    public FormattedString text(String text) {
        sb.append(escapeHTML(text));
        return this;
    }

    /**
     * Add bold text
     */
    public FormattedString bold(String text) {
        sb.append("<b>").append(escapeHTML(text)).append("</b>");
        return this;
    }

    /**
     * Add italic or emphasized text
     */
    public FormattedString italic(String text) {
        sb.append("<i>").append(escapeHTML(text)).append("</i>");
        return this;
    }

    /**
     * Add monospaced, or text with equal distance between each character
     */
    public FormattedString monospaced(String text) {
        sb.append("<code>").append(escapeHTML(text)).append("</code>");
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}

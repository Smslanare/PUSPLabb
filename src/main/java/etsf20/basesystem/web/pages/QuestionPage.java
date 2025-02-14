package etsf20.basesystem.web.pages;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;
import etsf20.basesystem.web.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Question page to display multiple options for a user to select
 */
public class QuestionPage extends Page {
    private final String title;
    private final String contentHtml;
    private final List<ButtonOption> options;

    /**
     * Option for a user to select
     * @param option label of button
     * @param style how it should be rendered
     */
    public record ButtonOption(String option, ButtonStyle style) {
        public String bootstrapClass() {
            return switch (style) {
                case DEFAULT -> "btn-default";
                case DANGER -> "btn-danger";
                case SUCCESS -> "btn-success";
                case PRIMARY -> "btn-primary";
                case SECONDARY -> "btn-secondary";
            };
        }
    }

    public enum ButtonStyle {
        DEFAULT,
        PRIMARY,
        SUCCESS,
        DANGER,
        SECONDARY
    }

    /**
     * Question page builder
     */
    public static class Builder {
        private String title = "";
        private FormattedString content = new FormattedString();
        private final List<ButtonOption> options = new ArrayList<>();

        private Builder() {
        }

        /**
         * Title of question page
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Formatted content
         */
        public Builder content(FormattedString formatted) {
            this.content = formatted;
            return this;
        }

        /**
         * Plain-text content without formatting
         */
        public Builder content(String text) {
            this.content = new FormattedString().text(text);
            return this;
        }

        /**
         * Default button
         * @param option label of button, e.g. Yes, No
         */
        public Builder option(String option) {
            options.add(new ButtonOption(option, ButtonStyle.DEFAULT));
            return this;
        }

        /**
         * Button with a particular style
         * @param option label of button, e.g. Yes, No
         * @param style button style
         */
        public Builder option(String option, ButtonStyle style) {
            options.add(new ButtonOption(option, style));
            return this;
        }

        /**
         * Create page from given settings
         * @param ctx context
         * @return page
         */
        public QuestionPage create(Context ctx) {
            return new QuestionPage(ctx, title, content.toString(), options);
        }
    }

    /**
     * Create a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private QuestionPage(Context ctx, String title, String contentHtml, List<ButtonOption> options) {
        super(ctx);
        this.title = title;
        this.contentHtml = contentHtml;
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public List<ButtonOption> getOptions() {
        return options;
    }

    public void render() {
        this.render("pages/utility/question.jte");
    }

    /**
     * Read selected option
     * @param ctx context
     * @return some choice if a form has been posted, @@ERROR if invalid form, none if no form
     * @throws ValidationException no choice or missing form parameter
     */
    public static String getChoice(Context ctx) throws ValidationException {
        if(ctx.method() == HandlerType.POST) {
           String value = ctx.formParam("choice");
           if(value == null) {
               throw new ValidationException("Invalid form", "choice could not be read");
           }
           return value;
        }

        throw new ValidationException("Invalid form", "choice has not been submitted");
    }
}

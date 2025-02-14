@param etsf20.basesystem.web.pages.QuestionPage page

@template.layout.page(
title = "Question",
page = page,
content = @`
    <h3>${page.getTitle()}</h3>
    <form method="post" action="${page.fullPath()}">
        <div class="mb-12 pb-3">
            $unsafe{page.getContentHtml()}
        </div>
        <div class="mb-12">
            @for(var option : page.getOptions())
                    <input type="submit" class="btn ${option.bootstrapClass()}" name="choice" value="${option.option()}">
            @endfor
        </div>

    </form>
`)
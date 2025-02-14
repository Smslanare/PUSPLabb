@param etsf20.basesystem.web.pages.Page page

@template.layout.page(
  title = "Start",
  page = page,
  content = @`
  <h1>Welcome ${page.session().displayName()}!</h1>
`)
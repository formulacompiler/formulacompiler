
@head_nodes << '<link rel="stylesheet" type="text/css" media="screen" href="' + root_path + 'style_screen.css" />'
@head_nodes << '<link rel="stylesheet" type="text/css" media="print" href="' + root_path + 'style_print.css" />'

@rootcrumb = '<a href="http://arrenbrecht.ch/">arrenbrecht.ch</a>'
@crumbs << 'SEJ'

# Redirect to output path and enforce it exists.
@html_name = '../../doc/' + html_name
FileUtils.mkpath File.dirname( html_name )


@head_nodes << '<link rel="stylesheet" type="text/css" media="screen" href="' + root_path + 'style_screen.css" />'
@head_nodes << '<link rel="stylesheet" type="text/css" media="print" href="' + root_path + 'style_print.css" />'

@crumbs << 'AFC'

# Redirect to output path and enforce it exists.
@html_path = '../../temp/doc/'
@html_name = @html_path + html_name
FileUtils.mkpath File.dirname( html_name )

@head_nodes << '<link rel="stylesheet" type="text/css" media="screen" href="' + root_path + 'style_screen.css" />'
@head_nodes << '<link rel="stylesheet" type="text/css" media="print" href="' + root_path + 'style_print.css" />'

@site_title = 'Abacus Formula Compiler for Java'

add_topics([
	:quick, 'Quick Start',
	:doc_, 'Documentation',
	:download_, 'Download',
	:support_, 'Support',
	:contribute_, 'Contribute' ])

@toc = nil

@crumbs << 'Home'

# Redirect to output path and enforce it exists.
@html_path = '../../temp/doc/'
@html_name = @html_path + html_name
FileUtils.mkpath File.dirname( html_name )


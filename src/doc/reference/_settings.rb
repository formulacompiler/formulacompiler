@crumbs << 'Reference'

@head_nodes << '<link rel="stylesheet" type="text/css" href="refstyle.css" />'


def gen_doc( basename )
	fn = '../../components/system/temp/test-reference/doc/' + basename + '.htm'
	if File.exists?( fn )
		return "<notextile>\n" + read_file( fn ) + "</notextile>\n\n"
	else
		warn "File not found: " + fn
	end
end


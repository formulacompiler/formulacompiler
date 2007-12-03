
# Build a page index of all pages matching @all_like@, except for those matching @except_like@.
# 
def index_all_except( all_like, except_like )
	index = []
	dir = File.dirname @rextile_name
	paths = glob( File.join( dir, '*.rextile' ) )
	paths = paths.find_all {|path| path =~ all_like }.reject {|path| path =~ except_like }
	files = paths.map {|path| [File.new(path), File.basename(path)] }
	files = files.sort_by {|file, name| name }

	files.each do |file, name|
		path = file.path
		puts '  * ' + path

		process path # make sure it is fully processed so we can load its DOM

		html_path = @html_path + path.chomp( '.rextile' ) + '.htm'
		dom = Hpricot( read_file( html_path ))

		title = (dom%:h1).inner_html
		teaser = (dom%:p).inner_html
		link = File.basename( html_path )

		index << "\t* <a href=\"#{link}\">#{title}</a> - "
		index << "#{teaser} <span class=\"when\">...<a href=\"#{link}\">more</a></span>\n\n"
	end
	index
end


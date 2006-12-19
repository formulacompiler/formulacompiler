
# Build a page index of all pages matching @all_like@, except for those matching @except_like@.
# 
def index_all_except( all_like, except_like )
	index = []
	dir = File.dirname @rextile_name
	paths = Dir.glob( File.join( dir, '*.rextile' ) )
	paths = paths.find_all {|path| path =~ all_like }.reject {|path| path =~ except_like }
	files = paths.map {|path| [File.new(path), extract_date_from(File.basename(path))] }
	files = files.sort_by {|file, date| date }.reverse

	curr_month = DateTime.new( 2099, 1, 1 )
	files.each do |file, date|
		path = file.path
		puts '  * ' + path

		process path # make sure it is fully processed so we can load its DOM

		html_path = '../../doc/' + path.chomp( '.rextile' ) + '.htm'
		dom = Hpricot( read_file( html_path ))

		title = (dom%:h1).inner_html
		teaser = (dom%:p).inner_html
		link = File.basename( html_path )

		if date < curr_month
			month_title = date.strftime( '%B %Y' )
			index << "\nh2. #{month_title}\n\n"
			curr_month = date.start_of_month
		end

		timestamp = date.strftime( '%b %d' )
		index << "h4. #{timestamp}\n\n\"#{title}\":#{link} - "
		index << "#{teaser} <span class=\"when\">...<a href=\"#{link}\">more</a></span>\n\n"
	end
	index
end


def extract_date_from( name )
	name =~ /(\d*)-(\d*)-(\d*)_.*/
	Date.new( $1.to_i, $2.to_i, $3.to_i )
end


def summary( title, file, term_list_files )
	terms = []
	term_list_files.each do |fn|
		fn = '../../build/temp/reference/' + fn + '_terms.rb'
		File.open( fn, 'r' ) { |f| eval f.read, binding }
	end
	terms.sort!
	terms.uniq!
	term_list = terms.map { |t| '@' + t + '@' }.join( ', ' )
	
	"<dt><a href=\"#{file}.htm\">#{title}</a></dt><dd>#{term_list}</dd>\n"
end

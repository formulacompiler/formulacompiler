
class Rextile::Processor

	def summary( title, file, term_list_files )
		terms = []
		term_list_files.each do |fn|
			fn = '../../components/system/temp/test-reference/doc/' + fn + '_terms.rb'
			begin
				File.open( fn, 'r' ) { |f| eval f.read, binding }
			rescue
				terms << "[Failed to open #{fn}]"
				warn "Failed to open #{fn}."
			end
		end
		terms.sort!
		terms.uniq!
		term_list = terms.map { |t| '@' + t + '@' }.join( ', ' )
		
		"<dt><a href=\"#{file}.htm\">#{title}</a></dt><dd>#{term_list}</dd>\n"
	end
	
end
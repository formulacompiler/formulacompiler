require 'fileutils'

def read_file( path )
  File.open( path, "r" ) {|file| file.read }
end

def write_file( path, content ) 
	puts "  -> " + path
	File.open( path, "w" ) {|file| file.write content }
end

old_header = read_file( '../copyright-header.old.txt' )
old_len = old_header.length

new_header = read_file( '../copyright-header.txt' )

files = [ ".java", ".jj", ".template" ].map{ |e| Dir.glob( "../../**/src/**/*" + e ) }.flatten

files.each do |file|
	txt = read_file( file )
	if txt[0,old_len] == old_header
		txt = new_header + txt[ old_len, txt.length ]
		write_file( file, txt )
	end
end

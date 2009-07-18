require 'fileutils'

def read_file( path )
  File.open( path, "r" ) {|file| file.read }
end

def write_file( path, content ) 
	puts "  -> " + path
	File.open( path, "w" ) {|file| file.write content }
end

def subst( s, src, tgt )
	s = s.gsub( '":' + src, '":' + tgt )
	s = s.gsub( ']' + src, ']' + tgt )
	return s
end

def process( spec, map )
	files = [ ".rextile", ".rextinc" ].map{ |e| Dir.glob( "src/doc" + spec+ "*" + e ) }.flatten
	files.each do |file|
		old = read_file( file )
		new = old
		map.each{ |src, tgt| new = subst( new, src, tgt ) }
		write_file( file, new ) unless old == new
		#puts "Modified #{file}" unless old == new
	end
end


process( "/",
	{	"doc/tutorial/" => "doc/doc/tutorial/",
	})

process( "/download/**/",
	{	"tutorial/" => "../doc/tutorial/",
		"reference/" => "../doc/reference/",
		"javadoc/" => "../doc/javadoc/",
		"impl/" => "../doc/impl/",
		"hacking/" => "../contribute/hacking/",
		"dev/" => "../contribute/journal/",
		"limit" => "../doc/limit",
		"road" => "../doc/road",
	})

process( "/contribute/hacking/",
	{	"../tutorial/" => "../../doc/tutorial/",
		"../reference/" => "../../doc/reference/",
		"../javadoc/" => "../../doc/javadoc/",
		"../impl/" => "../../doc/impl/",
		"../dev/" => "../journal/",
		"../limit" => "../../doc/limit",
		"../road" => "../../doc/road",
	})

process( "/contribute/journal/",
	{	"../tutorial/" => "../../doc/tutorial/",
		"../reference/" => "../../doc/reference/",
		"../javadoc/" => "../../doc/javadoc/",
		"../impl/" => "../../doc/impl/",
		"../limit" => "../../doc/limit",
		"../road" => "../../doc/road",
	})

process( "/doc/tutorial/",
	{	"../hacking/" => "../../contribute/hacking/",
		"../dev/" => "../../contribute/journal/",
		"../release" => "../../download/release",
	})

process( "/doc/reference/",
	{	"../hacking/" => "../../contribute/hacking/",
		"../dev/" => "../../contribute/journal/",
		"../release" => "../../download/release",
	})


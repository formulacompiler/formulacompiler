require 'erb'


class Project
		
	def initialize
		super()
		yaml = YAML::load( File.open( 'components.yaml' ))
		@comps = []
		@comps_by_name = {}
		@groups = yaml['groups'].map {|y| Group.new( self, nil, y, '' ) }
	end
	
	attr_reader :groups, :comps, :comps_by_name
	
	def require_comp_by_name( name )
		result = comps_by_name[ name ]
		throw "#{name} not found!" if result == nil
		result
	end

end


class Element
	
	def initialize( project, group, yaml, name_prefix )
		super()
		@id = yaml['id']
		@desc = yaml['desc']
		@notes = yaml['notes']
		@unchecked = yaml['unchecked']
		@color = yaml['color']
		@project = @project
		@group = group
		@name = name_prefix + id
	end
	
	attr_reader :id, :desc, :notes, :unchecked, :color
	attr_reader :name, :group, :project
				
end


class Group < Element
	
	def initialize( project, group, yaml, name_prefix )
		super( project, group, yaml, name_prefix )
		sub_prefix = name + '.'
		@comps = (yaml['comps'] || []).map { |y| Comp.new( project, self, y, sub_prefix ) }
		@groups = (yaml['groups'] || []).map {|y| Group.new( project, self, y, sub_prefix ) }
		@jar = yaml['jar']
	end
	
	attr_reader :groups, :comps, :jar
		
end	


class Comp < Element
	
	def initialize( project, group, yaml, name_prefix )
		super( project, group, yaml, name_prefix )
		@pkgs = yaml['pkgs'] || []
		@jars = yaml['jars'] || []
		@api_deps = (yaml['api_deps'] || []).map { |n| project.require_comp_by_name( n ) }
		@deps = (yaml['deps'] || []).map { |n| project.require_comp_by_name( n ) }
		project.comps << self
		project.comps_by_name[ name ] = self
	end
	
	attr_reader :pkgs, :jars, :api_deps, :deps
		
	def imported_pkgs
		result = []
		api_deps.each { |d| result += d.api_pkgs }
		deps.each { |d| result += d.api_pkgs }
		result.uniq
	end
	
	def api_pkgs
		result = []
		result += pkgs
		api_deps.each { |d| result += d.api_pkgs }
		result.uniq
	end
end


module SimpleReader

	def read_file( path )
		File.open( path, "r" ) {|file| file.read }
	end
	
end

module SimpleWriter
	include SimpleReader

	def write_file( path, content ) 
		current = if File.exists?( path ) then read_file( path ) else "" end
		unless current == content
			puts "  -> " + path
			File.open( path, "w" ) {|file| file.write content }
		end
	end

end

module Generator
	include SimpleWriter
	
	def subst_file( template_path, target_path )
		src = read_file( template_path )
		erb = ERB.new( src.untaint )
		erb.filename = template_path
		gen = erb.result( binding )
		write_file( target_path, gen )
	end
		
	def copy_file( src_path, gen_path )
		src = read_file( src_path )
		write_file( gen_path, src )
	end
	
end	


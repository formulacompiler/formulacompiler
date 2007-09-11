require 'yaml'
require 'project.rb'


class OverviewGenerator
	include SimpleWriter

	def initialize
		@project = Project.new
	end
	
	attr_reader :project
	
	def run
		write_file '../../temp/rextile/hacking/components.rextinc', gen
	end
	
	def gen
		rex = ''
		project.groups.reject{ |g| g.unchecked }.each { |g| rex += gen_subsys g }
		rex
	end
	
	def gen_subsys( g )
		rex = "\nh3. #{beautify(g.id)}\n\n#{[g.desc, g.notes].join(' ')}\n"
		g.groups.each { |g| rex += gen_jar g }
		rex
	end
	
	def gen_jar( g )
		rex = "\ndt. #{beautify(g.id)} (#{g.jar}) : #{[g.desc, g.notes].join(' ')}\n"
		rex
	end
	
	def beautify( id )
		return 'API' if id == 'api' 
		return 'JVM' if id == 'jvm' 
		return id.slice(0,1).upcase() + id.slice(1, id.length - 1)
	end
	
end

OverviewGenerator.new.run

require 'yaml'
require 'project.rb'


class DotGenerator
	include SimpleWriter

	def initialize
		@project = Project.new
	end
	
	attr_reader :project
	
	def run
		write_file '../../temp/rextile/hacking/dependency-graph.dot', gen
	end
	
	def gen
		dot = 'digraph "SEJ Component Overview" {' + "\n"
		dot += ' fontname = "Helvetica"; labeljust = "l"; fontsize = 8; node [ shape = box, fontname = "Helvetica", fontsize = 10, style = "filled" ]; edge [ fontname = "Helvetica", sametail="a" ];' + "\n"
		
		project.groups.each { |g| dot += gen_subgraph g }
		project.comps.each { |c| dot += gen_deps c }
		
		dot += '}' + "\n"
	end
	
	def gen_subgraph( g )
		dot = ''
		#dot = 'subgraph "cluster_' + g.id + '" {' + "\n"
		#dot += '  label = "Subsystem: ' + g.id + '";' + "\n"
		g.comps.each { |c| dot += gen_comp c }
		g.groups.each { |g| dot += gen_subgraph g }
		#dot += '}' + "\n"
		dot
	end
	
	def gen_comp( c )
		if c.jars.length > 0
			lines = c.jars
		else
			lines = c.pkgs.map{ |l| l.sub( "org.formulacompiler", ".." ) }
		end
		dot = '"' + c.name + '" [label = "' + lines.join( '\n' ) + '", color = "' + c.group.color + '"];' + "\n"
	end
	
	def gen_deps( c )
		dot = ''
		c.api_deps.each { |d| dot += '"' + c.name + '" -> "' + d.name + '" [ color = red, weight = 2 ];' + "\n" }
		c.deps.each { |d| dot += '"' + c.name + '" -> "' + d.name + '";' + "\n" }
		dot
	end
	
end

DotGenerator.new.run

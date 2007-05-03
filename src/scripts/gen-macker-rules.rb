require 'yaml'
require 'project.rb'


class ImportGenerator
	include Generator

	def initialize
		@project = Project.new
		@checked_packages = []
	end
	
	attr_reader :project, :packages, :checked_packages

	def run
		subst_file 'macker-rules-template.xml', '../macker-rules.xml'
	end
	
end


ImportGenerator.new.run

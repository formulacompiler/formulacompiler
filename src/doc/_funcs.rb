# Global utility functions for Rextile.

def inc_file(name)
    read_file name
rescue
    msg = "#{name} missing in #{Dir.getwd}? Error was #{$!}."
    warn msg
    msg
end

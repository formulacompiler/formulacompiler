# Initialize template-specific variables.

@xhtml_header = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">'

# If set, defines the value for <head><title>. Otherwise, the text of the first <h1> is used.
@page_title = nil

# If set, defines the title for the entire site. Appended to page titles unless page title = site title.
@site_title = nil

# Use "@crumbs << 'mysection'" in your per-folder _settings.rb file to define the caption 
# for the breadcrumbs backlink.
@crumbs = []

# Use "@rootcrumb = '<a href="http://my.extroot.org/">External Root</a>'" to define a root crumb which is not built automatically.
@rootcrumb = nil

# Defines the sub-topics for the left-hand navigation menu.
@topic_lists = []
@topic_path = nil
add_topics([ :index, 'Home' ])

# Use "<% @style << '...' %>" in documents to add custom style definitions to <head><style>.
@styles = []

# Use "<% @body_attrs << '...' %>" in documents to add custom attributes to the <body> tag.
# Useful to add, for example, "onLoad" event code.
@body_attrs = []

# Use <% @head_nodes << '...' %> in documents to add custom header nodes (into <head>..</head>).
@head_nodes = []


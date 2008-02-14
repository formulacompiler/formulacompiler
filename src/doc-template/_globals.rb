# Global utility functions for Rextile template.


class Topic
	def initialize( id, title )
		@id = id
		n = id.to_s
		if n[-1] == '_'[0]
			@path = n[ 0, n.length - 1 ]
			@target = File.join( @path, 'index.htm' )
			@is_sub = true
		else
			@path = nil
			@target = n + '.htm'
			@is_sub = false
		end
		@title = title
		@is_current = false
		@is_parent = false
	end

	attr_reader :id, :target, :title, :path
	attr_accessor :is_current, :is_parent

	def to_s()
		"#{target} (#{title})"
	end
	def inspect()
		to_s
	end
end

def add_topics( args )
	list = []
	dict = {}
	id = nil
	args.each do |arg|
		if id
			topic = Topic.new( id, arg )
			list << topic
			dict[ id ] = topic
			id = nil
		else
			id = arg
		end
	end
	@topic_lists << [ list, dict ]
end

def topic_path()
 	unless @topic_path
	 	@topic_path = []
		
		path =  rextile_name
		tls = @topic_lists
		if path == 'index.rextile'
			ts, td = @topic_lists[ 0 ]
			t = ts[0]
			t.is_current = true
			@topic_path << t
		else
			ids = []
			p = path
			bn = File.basename( p )
			if bn != 'index.rextile'
				id = bn[ 0, bn.length - 8 ].to_sym # drop .rextile extension
				ids << id
			end
			p = p[ 0, p.length - bn.length - 1 ]
			while p and p != ''		
				bn = File.basename( p )
				id = (bn + '_').to_sym
				ids << id
				p = p[ 0, p.length - bn.length - 1 ] if p
			end
			ids << :index
			ids.reverse!
			
			tls = @topic_lists
			#puts ids.inspect
			#puts tls.inspect
			
			last_id = ids.length - 1
			for i in 0 .. [tls.length - 1, last_id].min
				id = ids[ i ]
				ts, td = tls[ i ]
				t = td[ id ]
				if i == last_id
					t.is_current = true
				else
					t.is_parent = true
				end
				@topic_path << t
			end
		end
	end
	return @topic_path
end


# Returns only the pure text content of the given node.
#
def text_only( node )
	txt = ''
	node.traverse_text{ |tn| txt << tn.content } unless node == nil
	txt
end


# Returns the area links for the given page.
#
def area_links( sep )
	topic_path()
	links = []
	ts, td = @topic_lists[1]
	ts.each do |t|
		tgt = t.target
		tgt = File.join( root_path, tgt ) unless root_path == ''
		if t.is_current
			lk = "<span class=\"selected\">#{t.title}</span>"
		else
			lk = "<a href=\"#{tgt}\">#{t.title}</a>"
			lk = "<span class=\"selected\">" + lk + "</span>" if t.is_parent
		end
		links << lk
	end
	links.join( sep )
end


# Returns the content links for the given page. Includes in-area links and the page's own TOC.
#
def content_links()
	tpath = topic_path()
	if tpath[0].is_current
		return ''
	else
		t = tpath[1]
		links = []
		if t.path
			path = root_path
			path = File.join( path, t.path ) if t.path
			content_links_of( t, tpath, 2, path, links )
		else
			links << '<div id="page_toc">' + @toc + '</div>' if @toc
		end
		return '<div id="area_toc">' + links.join( "\n" ) + '</div>'
	end
end

def content_link( t, tpath, at, path, links )
	if t.is_current
		links << '<div class="area"><span class="selected">' + t.title + '</span></div>'
	else
		tgt = File.join( path, t.target )
		lk = '<a href="' + tgt + '">' + t.title + '</a>'
		lk = '<span class="selected">' + lk + '</span>' if t.is_parent
		links << '<div class="area">' + lk + '</div>'
	end
	if t.is_current
		links << '<div id="page_toc">' + @toc +'</div>' if @toc
	end
	if t.path and (t.is_parent or t.is_current)
		path = File.join( path, t.path ) if t.path
		links << '<div class="nested">'
		content_links_of( t, tpath, at + 1, path, links )
		links << '</div>'
	end
end
		
def content_links_of( t, tpath, at, path, links )	
	if at < @topic_lists.length
		ts, td = @topic_lists[ at ]
		ts.each do |t|
			content_link( t, tpath, at, path, links )
		end
	elsif t.is_parent
		links << '<div class="area"><span class="selected">' + @page_title + '</span></div>'
		links << '<div id="page_toc">' + @toc + '</div>' if @toc
	end
end


# Formats the breadcrumbs accumulated in @crumbs into a series of links, separated by the
# given separator. The last crumb is omitted of the current page's name is "index.rextile".
#
def breadcrumbs( separator )
  res = ''
  pre = ''
  n = @crumbs.length
  if File.basename( rextile_name ) == 'index.rextile'
    n -= 1
    pre = '../'
  end
  if n > 0
    i = n - 1
    while i >= 0
      crumb = @crumbs[ i ]
      if crumb.kind_of?( Array )
        target = crumb[ 1 ]
        crumb = crumb[ 0 ]
      else
        target = pre + 'index.htm'
        pre += '../'
      end
      lk = '<a href="' + target + '">' + crumb + '</a>'
      lk += separator unless res == ''
      res = lk + res
      i -= 1
    end
  end
  if @rootcrumb
	res = separator + res unless res == ''
	res = @rootcrumb + res
  end
  res
end


############### OLD STUFF


# Inserts a Rextile XHTML script node which will later build a table of contents of the range of
# headers between "from" and "to". Headers with no existing anchor are given one using the
# specified prefix.
#
# The TOC is enclosed in a <div class="toc">..</div> tag. The individual links are organized
# as nested <ul>s. Each <ul> has the class "toc" and "toc<n>", where <n> is the header level.
# List items with children have the class "withitems". So:
#
#	<div class="toc">
# 		<ul class="toc toc2">
#			<li><a href="#pagetoc__1">Book 1</a></li>
#			<li class="withitems"><a href="#pagetoc__2">Book 2</a>
#				<ul class="toc toc3">
#					<li><a href="#pagetoc__2_1">Chapter 2.1</a></li>
#					<li><a href="#pagetoc__2_2">Chapter 2.2</a></li>
#				</ul>
#			</li>
#			<li><a href="#pagetoc__3">Book 3</a></li>
#		</ul>
#	</div>
#
def toc( from = 2, to = 3, anchor_prefix = 'pagetoc__' )
  "<pre class=\"rscript\">xtoc_marker #{from}, #{to}, '#{anchor_prefix}'</pre>"
end

def xtoc_marker( from, to, anchor_prefix )
  @toc = xtoc( from, to, anchor_prefix )
  return ''
end

def xtoc( hfrom, hto, anchor_prefix )
  wantTag = 'h' + hfrom.to_s
  subs = subs_of( html_script_node, hfrom, hto, [] )
  unless subs == nil
    hs, stops = subs
    '<div class="toc">' + make_toc( hs, hfrom, hto, anchor_prefix, 1, stops ) + '</div>'
  else
    ''
  end
end

def subs_of( h, hfrom, hto, stop_tags )
  hscan = hfrom
  while hscan <= hto
    want_tag = 'h' + hscan.to_s
    subs = list_of_subs_of( h, stop_tags, want_tag )
    if subs.length > 0
      return [subs, stop_tags + [want_tag]]
    end
    stop_tags += [want_tag]
    hscan += 1
  end
  nil
end

def make_toc( hs, hfrom, hto, anchor_prefix, toc_level, collected_tags )
  anchor_number = 1
  toc = ''
  for h in hs 
    if h.parent 
      anchor = anchor_prefix + anchor_number.to_s

      sub_toc = ''
      stop_tags = collected_tags
      hscan = hfrom + 1
      sub_level = toc_level + 1
      while hscan <= hto 
        want_tag = 'h' + hscan.to_s
        subs = list_of_subs_of( h, stop_tags, want_tag )
        if subs.length > 0 
          sub_toc = make_toc( subs, hscan, hto, anchor + "_", sub_level, stop_tags + [want_tag] )
          break
        end
        stop_tags += [want_tag]
        sub_level += 1
        hscan += 1
      end
      
      hentry = make_toc_entry( h, anchor )
      if sub_toc == ""
        toc += '<li>' + hentry + '</li>'
      else
        toc += '<li class="withitems">' + hentry + sub_toc + '</li>'
      end
      anchor_number += 1
    end
  end
  '<ul class="toc toc' + toc_level.to_s + '">' + toc + '</ul>';
end

def make_toc_entry( node, anchor )
  node_html = ""
  chd = node.children.first
  if not chd.text? and chd.name.downcase() == "a"
    node_html = chd.inner_html
    anchor = chd.get_attribute( "name" )
  else
    node_html = node.inner_html
    node.inner_html = '<a name="' + anchor + '">' + node_html + '</a>'
  end
  node_html.gsub!( /\<br\s*\/\>/i, '' )
  '<a href="#' + anchor + '">' + node_html + '</a>'
end

def list_of_subs_of( node, stop_tags, want_tag )
  hs = []
  nodes = node.parent.children
  at = nodes.index( node ) + 1
  while at < nodes.length
    node = nodes[ at ]
    unless node.text?
      atTag = node.name.downcase
      if atTag =~ /h.*/
        break if stop_tags.index( atTag )
        hs += [node] if want_tag == atTag;
      end
    end
    at += 1
  end
  hs
end


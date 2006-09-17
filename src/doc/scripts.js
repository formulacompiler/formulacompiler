function body_loaded()
{
	makePageToc();
}

function makePageToc( anchor_prefix )
{
	replaceByToc( document.getElementById( "pagetoc" ), "pagetoc_" );
}

function replaceByToc( tocNode, anchor_prefix )
{
	var hfrom = Number( tocNode.getAttribute( "hfrom" ));
	var hto = Number( tocNode.getAttribute( "hto" ));
	var wantTag = "h" + hfrom;
	var subs = subsOf( tocNode.parentNode, hfrom, hto, "" );
	if( subs ) {
		var hs = subs[0];
		var stops = subs[1];
		var tocHtml = makeToc( hs, hfrom, hto, anchor_prefix, 1, stops );
		tocNode.innerHTML = tocHtml;
	}
	else {
		tocNode.innerHTML = "";
	}
}

function subsOf( h, hfrom, hto, stopTags )
{
	var hscan = hfrom;
	while( hscan <= hto ) {
		var wantTag = "h" + hscan;
		var subs = listOfSubsOf( h, stopTags, wantTag );
		if( subs.length > 0 ) {
			return [subs, stopTags + wantTag];
		}
		stopTags += wantTag;
		hscan++;
	}
	return undefined;
}

function makeToc( hs, hfrom, hto, anchor_prefix, toc_level, collectedTags )
{
	var anchor_number = 1;
	var toc = "";
	for( var ih in hs ) {
		var h = hs[ ih ];
		if( h.parentNode ) {
			var anchor = anchor_prefix + anchor_number;

			var sub_toc = "";
			var stopTags = collectedTags;
			var hscan = hfrom + 1;
			var sub_level = toc_level + 1;
			while( hscan <= hto ) {
				var wantTag = "h" + hscan;
				var subs = listOfSubsOf( h, stopTags, wantTag );
				if( subs.length > 0 ) {
					sub_toc = makeToc( subs, hscan, hto, anchor + "_", sub_level, stopTags + wantTag );
					break;
				}
				stopTags += wantTag;
				sub_level++;
				hscan++;
			}
			
			hentry = makeTocEntry( h, anchor );
			if( sub_toc == "" ){
				toc += "<li>" + hentry + "</li>";
			}
			else {
				toc += "<li class=\"withitems\">" + hentry + sub_toc + "</li>";
			}
			anchor_number++;
		}
	}
	return "<ul class=\"toc toc" + toc_level + "\">" + toc + "</ul>";
}

function makeTocEntry( node, anchor )
{
	var nodeHtml = "";
	if( node.firstChild.tagName && node.firstChild.tagName.toLowerCase() == "a" ) {
		nodeHtml = node.firstChild.innerHTML;
		anchor = node.firstChild.getAttribute( "name" );
	}
	else {
		nodeHtml = node.innerHTML;
		node.innerHTML = "<a name=\"" + anchor + "\">" + nodeHtml + "</a>";
	}
	return "<a href=\"#" + anchor + "\">" + nodeHtml + "</a>";
}

function listOfSubsOf( node, stopTags, wantTag )
{
	var hs = [];
	while( node = node.nextSibling ) {
		var atTag = node.tagName;
		if( atTag ) {
			atTag = atTag.toLowerCase();
			if( atTag.charAt(0) == 'h' ) {
				if( stopTags.search( atTag ) >= 0 ) break;
				if( wantTag == atTag ) hs = hs.concat( node );
			}
		}
	}
	return hs;
}

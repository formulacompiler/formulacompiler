# Using Mercurial with AFC #

AFC uses the [Mercurial](http://selenic.com/mercurial) distributed version control system (DVCS) to manage its source code. This page tells you only the specifics of using Mercurial with AFC. So please refer to the [main Mercurial site](http://selenic.com/mercurial) for how to install and use Mercurial in general.


## Configuration ##

You should update your [.hgrc](http://www.selenic.com/mercurial/hgrc.5.html) file as follows:
```
[ui]
username=you@email.com

[diff]
git=true
```

The latter is important because AFC’s source contains a number of binary files which standard diff cannot handle.


## Repository Access ##

To get an initial copy of a repository, run
```
hg clone http://formulacompiler.org/hg/afc my-local-repo
```


## Submitting Changes ##

Unless you are a committer, you submit changes as patches you send to the [developer list](http://groups.google.com/group/formulacompiler-devel/). This is easy with the [hg email](http://www.selenic.com/mercurial/wiki/index.cgi/PatchbombExtension) command (be sure to configure it properly):
```
hg email -r tip -t formulacompiler-devel@googlegroups.com
```

Look at `hg help email` for details and how to send multiple patches in one go. Please send simple patches inline as it is far easier to comment on them that way. Patches or patch series containing large diffs (for instance, because you had to change Excel-based reference tests) should be sent as bundles.


## Getting Changes Accepted ##

Follow the [development guidelines](http://www.formulacompiler.org/contribute/hacking/index.htm).

Split your changes into small, targeted, self-contained patches. Don't lump unrelated changes together in one patch. In particular, separate coding-style cleanups from actual changes.

Ensure the tests run fine after every single patch you submit, even if you are submitting a series of incremental patches.

Expect to have to improve your patches a few times before they get accepted. Don't take this as personal criticism, even if responses are terse.

If a patch goes unnoticed, resend it after a while.

We have started to prefix commit messages with subsystem/area: run(time), comp(iler), xls, odf, doc, api (for general stuff), build. You can leave it off if the area is not clear or the patch touches too many areas.


## Using Mercurial Patch Queues ##

It sounds daunting to have to maintain a change as a series of incremental patches and to be ready to improve every one of them as needed. The [Mercurial Queue extension](http://www.selenic.com/mercurial/wiki/index.cgi/MqExtension), however, makes it fairly easy. The key `hg` commands are `qnew`, `qrefresh`, `qpop`, and `qpush`. You may also want to look at the [extensive tutorial](http://hgbook.red-bean.com/hgbookch12.html#x16-26500012) and the page on [rebasing your patches](http://www.selenic.com/mercurial/wiki/index.cgi/MqMerge) on a newer upstream version.
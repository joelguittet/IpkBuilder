IpkBuilder
==

Java application that creates IPK packages on Windows or Unix

This tool creates IPK packages from an existing directory structure. Control scripts "preinst", "postinst", "prerm" and "postrm" can be specified and integrated to the package.


Usage
--

	following options are available:
	-n,--name <name>                   Package name
	-v,--version <version>             Package version
	-a,--arch <architecture>           Package architecture
	-dep,--depends <depends>           Package depends
	-maint,--maintainer <maintainer>   Package maintainer
	-desc,--desc <description>         Package description
	-preinst,--preinst <preinst>       Package pre-installation script path
	-postinst,--postinst <postinst>    Package post-installation script path
	-prerm,--prerm <prerm>             Package pre-remove script path
	-postrm,--postrm <postrm>          Package post-remove script path
	-i,--input <input>                 Input path
	-o,--output <output>               Output Path

Unix, use writing:

	-jar com.github.myfreescalewebpage.ipkbuilder.jar -n <name> -v <version> -a <arch> -i "/root/path/of/data/to/be/packaged" -o "/destination/path/of/ipk"

Windows, use writing:

	-jar com.github.myfreescalewebpage.ipkbuilder.jar -n <name> -v <version> -a <arch> -i "C:\\root\\path\\of\\data\\to\\be\\packaged" -o "C:\\destination\\path\\of\\ipk"

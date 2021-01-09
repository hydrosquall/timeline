
.PHONY: build


build:
		lein cljsbuild once min

		# copy 3rd party resources to build directory
		mkdir -p resources/public/target/cljsbuild-compiler-1/
		cp -R target/cljsbuild-compiler-1/* resources/public/target/cljsbuild-compiler-1/

		# remove all non JS files
		find resources/public/target/cljsbuild-compiler-1/ -name '*.cljs' -delete
		find resources/public/target/cljsbuild-compiler-1/ -name '*.cache.json' -delete
		find resources/public/target/cljsbuild-compiler-1/ -name '*.js.map' -delete
		find resources/public/target/cljsbuild-compiler-1/ -name '*.cljc' -delete

sWangTiler is a java implementation that generates wang tiles from any given input image.
By assembling wang tiles that share common edges (like domino pieces) one may generate textures of any desired size.

Wang tiles greatly reduce visible repetitions making textures seem more natural and thus improve the quality of any graphic application like games, simulations etc.

This implementation is based on the strict wang tile algorithm as proposed by Xinyu Zhang and Young J. Kim in "Efficient Texture Synthesis Using Strict Wang Tiles". In order to calculate the lowest cost path between any edge- and sample image an implementation of Dijkstra's algorithm is used which was found in the java graph library 'JGraphT'.

This download includes a folder of images that proved to work very well with this application.

Credits to all mentioned parties and to http://www.texturemate.com/.
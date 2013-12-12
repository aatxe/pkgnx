package us.aaronweiss.pkgnx.test.util;

import us.aaronweiss.pkgnx.NXNode;

import java.util.HashMap;

/**
 * A {@code HashMap} that maps full paths to nodes.
 *
 * @author Aaron Weiss
 * @version 1.0.0
 * @since 7/10/13
 */
public class NodePathMap extends HashMap<String, NXNode> {
	public NodePathMap(NXNode root) {
		recurseConstruct(root, "");
	}

	private void recurseConstruct(NXNode node, String pathAccum) {
		pathAccum += "/" + node.getName();
		put(pathAccum, node);
		for (NXNode c : node)
			recurseConstruct(c, pathAccum);
	}
}

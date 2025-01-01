package com.guava;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    String name;
    List<TreeNode> children = new ArrayList<>();
    
    TreeNode(String name) {
        this.name = name;
    }
    
    void addChild(TreeNode child) {
        children.add(child);
    }
    
    void printTree(TreeNode node, String prefix, boolean isLast) {
        if (node.name.equals("Program")) {
            System.out.println(node.name);
        } else {
            // Print the current node
            System.out.println(prefix + (isLast ? "└── " : "├── ") + node.name);
        }

        
        // Iterate over children
        for (int i = 0; i < node.children.size(); i++) {
            printTree(node.children.get(i), prefix + (isLast ? "    " : "│   "), i == node.children.size() - 1);
        }
    }

    void buildTreeString(TreeNode node, String prefix, boolean isLast, StringBuilder builder) {
        if (node.name.equals("Program")) {
            builder.append(node.name).append("\n");
        } else {
            builder.append(prefix)
                   .append(isLast ? "└── " : "├── ")
                   .append(node.name)
                   .append("\n");
        }

        for (int i = 0; i < node.children.size(); i++) {
            buildTreeString(node.children.get(i), prefix + (isLast ? "    " : "│   "), i == node.children.size() - 1, builder);
        }
    }
}

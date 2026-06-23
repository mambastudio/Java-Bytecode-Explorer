package com.mamba.bytecodeexplorer.file;

public record TreeRelation<X, Y extends Tree<X,Y>> (Y tree, X parent, X child){
    private TreeRelation(){
        this(null, null, null);
    }

    public static<X, Y extends Tree<X,Y>> TreeRelation<X, Y> empty(){
        return new TreeRelation();
    }

    public static<X, Y extends Tree<X,Y>> TreeRelation<X, Y> asChild(Y y, X child){
        return new TreeRelation(y, null, child);
    }

    public boolean hasParent(){
        return parent() != null;
    }

    public boolean isPresent(){
        return child != null;
    }

    public boolean hasModel(){
        return tree != null;
    }
}

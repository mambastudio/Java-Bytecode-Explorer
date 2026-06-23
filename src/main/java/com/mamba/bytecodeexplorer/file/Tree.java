package com.mamba.bytecodeexplorer.file;

import javafx.collections.ObservableList;

public interface Tree<X, Y extends Tree<X,Y>> {
    X ref();
    ObservableList<Y> children();

    default TreeRelation<X, Y> findInTree(Y y){
        return findInTree(y.ref());
    }

    default TreeRelation<X, Y> findInTree(X x){
        return null;
    }

    default boolean remove(X x) {
        return false;
    }

    default boolean remove(Y y){
        return children().remove(y);
    }

    default boolean childrenContain(Y child){
        return children().contains(child);
    }

    default boolean childrenContain(X child){
        for(Y y : children()){
            if(y.ref().equals(child))
                return true;
        }
        return false;
    }

    default boolean removeChild(X child){
        int index = -1;
        for(int i = 0; i<children().size(); i++)
            if(children().get(i).ref().equals(child)){
                index = i;
                break;
            }
        return switch(index > -1){
            case true -> {
                children().remove(index);
                yield true;
            }
            default -> false;
        };
    }

    default boolean exist(Y y){
        var result = findInTree(y);
        return result.isPresent();
    }

    default boolean exist(X x){
        var result = findInTree(x);
        return result.isPresent();
    }

    default boolean hasChildren(){
        return !children().isEmpty();
    }
}

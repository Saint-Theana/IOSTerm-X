package io.github.sainttheana;

import java.util.ArrayList;
import java.util.List;

public class LimitedList<E> {
    private List<E> delegate;
    private int maxSize = 0;

    public LimitedList(int maxSize) {
        this.maxSize = maxSize;
        delegate = new ArrayList<>(maxSize + 1);
    }

    public boolean add(E e) {
        if (e == null || maxSize == 0) {
            return true;
        }
        delegate.add(e);
        if (delegate.size() > maxSize) {
            delegate.remove(0);
        }
        return false;
    }
	
	public E set(int index,E obj) {
        return delegate.set(index,obj);
    }

    public E remove(int index) {
        return delegate.remove(index);
    }

    public E get(int index) {
            return delegate.get(index);
    }

    public int size() {
        return delegate.size();
    }
	
	public void clear(){
		delegate.clear();
	}
}


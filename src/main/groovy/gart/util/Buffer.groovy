/*
 * Copyright 2014 Matt Dean
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package gart.util

/**
 * A generic buffer that can store stuff.
 * To limit the buffer size, use Buffer(int)
 * To enable overflow when size limit is hit, use Buffer(int, true)
 *
 * @author deanydean
 */
public class Buffer implements Queue {

    // Use a basic linked list for storage
    private storage = new LinkedList()

    // Do not limit size of buffer by default
    private maxSize = null

    // Disable buffer overflow
    public overflow = false

    public Buffer(){
    }

    public Buffer(maxSize){
        this.maxSize = maxSize
    }

    public Buffer(maxSize, overflow){
        this.maxSize = maxSize
        this.overflow = overflow
    }

    public boolean addAll(Collection c){
        return this.storage.addAll(c)
    }

    public boolean add(e){
        def added = this.offer(e)

        if(!added)
            throw new IllegalStateException("Element not added")

        return true
    }

    public void clear(){
        this.storage.clear()
    }

    public boolean contains(o){
        return this.storage.contains(o)
    }

    public boolean containsAll(Collection c){
        return this.storage.containsAll(o)
    }

    public element(){
        return this.storage.element()
    }

    public boolean equals(o){
        if(o == this)
            return true

        if(!(o instanceof Buffer))
            return false
        
        return this.storage.equals(o.storage) && this.maxSize == o.maxSize &&
            this.overflow == o.overflow
    }

    public int hasCode(){
        return "${this.storage.hashCode()}${this.maxSize}${this.overflow}"
            .hashCode()
    }

    public boolean isEmpty(){
        return this.storage.isEmpty()
    }

    public Iterator iterator(){
        return this.storage.iterator()
    }

    public boolean offer(e){
        if(this.maxSize && this.storage.size() >= this.maxSize){
            if(this.overflow)
                this.storage.remove()
            else
                return false
        }

        this.storage << e
        return true

    }

    public peek(){
        return this.storage.peek()
    }

    public poll(){
        return this.storage.poll()
    }

    public remove(){
        return this.storage.remove()
    }

    public boolean remove(o){
        return this.storage.remove(o)
    }

    public boolean removeAll(Collection c){
        return this.storage.removeAll(c)
    }

    public boolean retainAll(Collection c){
        return this.storage.retainAll(c)
    }

    public int size(){
        return this.storage.size()
    }

    public Object[] toArray(){
        return this.storage.toArray()
    }

    public Object[] toArray(Object[] a){
        return this.storage.toArray(a)
    }
}

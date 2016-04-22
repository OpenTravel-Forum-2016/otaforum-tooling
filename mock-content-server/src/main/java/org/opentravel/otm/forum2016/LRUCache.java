/**
 * Copyright (C) 2016 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.otm.forum2016;

import java.util.HashMap;

/**
 * Generic implementation of a least recently used (LRU) cache.
 * 
 * @param <C> the type of object managed by the cache
 * @author S. Livezey
 */
public class LRUCache<C extends Cacheable> {
	
	public static final int DEFAULT_CAPACITY = 25;
	
	private int capacity;
	private HashMap<String,Node> cache = new HashMap<>();
	private Node head = null;
	private Node end = null;
	
	
	/**
	 * Default constructor.
	 */
	public LRUCache() {
		this(DEFAULT_CAPACITY);
	}
	
	/**
	 * Constructor that specifies the capacity of the cache.
	 * 
	 * @param capacity the maximum size of the cache
	 */
	public LRUCache(int capacity) {
		this.capacity = capacity;
	}
	
	/**
	 * Returns the item from the cache with the given key.
	 * 
	 * @param key  the key for which to return the associated cached item
	 * @return C
	 */
	public C get(String key) {
		C item = null;
		
		if (cache.containsKey( key )) {
			Node n = cache.get( key );
			
			remove(n);
			setHead(n);
			item = n.item;
		}
		return item;
	}
	
	/**
	 * Adds the given item to the cache.
	 * 
	 * @param item  the cacheable item to add
	 */
	public void add(C item) {
		String key = item.getCacheKey();
		
		if (cache.containsKey(key)) {
			Node old = cache.get(key);
			
			old.item = item;
			remove(old);
			setHead(old);
			
		} else {
			Node created = new Node(item);
			
			if (cache.size() >= capacity) {
				cache.remove( end.item.getCacheKey() );
				remove(end);
				setHead(created);
				
			} else {
				setHead(created);
			}
			cache.put(key, created);
		}
	}
	
	/**
	 * Removes the item with the given key from the cache.
	 * 
	 * @param key  the key for the item to remove
	 */
	public void remove(String key) {
		remove( cache.get( key ) );
	}
	
	/**
	 * Clears the contents of this cache.
	 */
	public void clear() {
		cache.clear();
		head = null;
		end = null;
	}
	
	/**
	 * Removes the given node from the cache.
	 * 
	 * @param n  the node to be removed from the cache
	 */
	private void remove(Node n) {
		if (n != null) {
			if (n.pre != null) {
				n.pre.next = n.next;
			} else {
				head = n.next;
			}
			
			if (n.next != null) {
				n.next.pre = n.pre;
			} else {
				end = n.pre;
			}
		}
	}
	
	/**
	 * Assigns the given node as the head of the LRU list.
	 * 
	 * @param n  the node to assign as the LRU head
	 */
	private void setHead(Node n) {
		n.next = head;
		n.pre = null;
		
		if (head != null) {
			head.pre = n;
		}
		head = n;
		
		if (end == null) {
			end = head;
		}
	}
	
	/**
	 * Wrapper for individual items managed within the LRU cache.
	 */
	private class Node {
		
		public C item;
		Node pre;
		Node next;
		
		/**
		 * Constructor that supplies the cachable item to be wrapped.
		 * 
		 * @param item  the cachable item to be wrapped
		 */
		public Node(C item) {
			this.item = item;
		}
		
	}
	
}

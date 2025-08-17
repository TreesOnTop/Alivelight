package net.blixate.deadlight.util.random;

/** Interface to create drop tables.
 * 
 * <p>If used on it's own, all items in this table have an equal chance of appearing.</p>
 */
public interface DropTable<T> {
	public T pick();
}
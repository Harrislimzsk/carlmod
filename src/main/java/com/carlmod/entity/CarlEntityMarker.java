package com.carlmod.entity;

/**
 * Marker interface implemented by every "Carl" entity (Wild Carl, Tameable Carl, ...).
 * <p>
 * Two systems rely on this interface instead of hard-coded {@code instanceof} chains:
 * <ul>
 *     <li>{@code BigMouthStaffItem} — right-clicking anything implementing this interface
 *         triggers the teleport into the Carl Dimension.</li>
 *     <li>The erase-aura in {@code WildCarlEntity} (and its subclasses) — any entity
 *         implementing this interface is skipped so Carls never erase each other.</li>
 * </ul>
 */
public interface CarlEntityMarker {
}

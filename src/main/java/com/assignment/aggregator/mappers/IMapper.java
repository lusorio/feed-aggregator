package com.assignment.aggregator.mappers;

import java.util.Collection;
import java.util.List;

/**
 * Interface for ENTITY <--> DTO mappers
 * <p>
 * Delegate the mapping between objects, typically entities and one of its
 * representations (DTOs), to specialised mappers.
 * <p>
 * Specialised mappers may extend the main {@link Mapper} class and, optionally, override one
 * or more of its methods depending on the mapping strategy adopted for each entity.
 *
 * @param <E> the entity
 * @param <D> the dto
 */
public interface IMapper<E, D>
{
    /**
     * Map DTO to an entity
     *
     * @param source          the DTO to convert
     * @param destinationType the destination class type of the entity
     * @return the entity
     */
    E map(D source, Class<E> destinationType);

    /**
     * Map DTO to an entity.
     *
     * @param source          the list of DTO to convert
     * @param destinationType the destination class type of the entity
     * @return the list of entities
     */
    List<E> map(Collection<D> source, Class<E> destinationType);

    /**
     * Map an entity to a DTO.
     *
     * @param source          the entity to convert
     * @param destinationType the destination class type of the DTO
     * @return the DTO
     */
    D mapToDTO(E source, Class<D> destinationType);

    /**
     * Map a list of entities to a list of its DTOs
     *
     * @param source          the list of entities to convert
     * @param destinationType the destination's class of the DTO
     * @return the list of DTOs
     */
    List<D> mapToDTO(Collection<E> source, Class<D> destinationType);
}

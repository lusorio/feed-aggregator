package com.assignment.aggregator.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper<E, D> implements IMapper<E, D>
{

    final ModelMapper modelMapper;

    public Mapper()
    {
        this.modelMapper = new ModelMapper();
    }

    @Override
    public E map(D source, Class<E> destinationType)
    {
        return modelMapper.map(source, destinationType);
    }

    @Override
    public D mapToDTO(E source, Class<D> destinationType)
    {
        return modelMapper.map(source, destinationType);
    }

    @Override
    public List<D> mapToDTO(Collection<E> source, Class<D> destinationType)
    {
        return source.stream()
                     .map(s -> this.mapToDTO(s, destinationType))
                     .collect(Collectors.toList());
    }
}

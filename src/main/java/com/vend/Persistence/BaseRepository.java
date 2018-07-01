package com.vend.Persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Created by makha on 28/06/2018.
 */
@NoRepositoryBean
public interface BaseRepository <k> extends JpaRepository<k, Integer> {

}


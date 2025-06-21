package com.chatbot.unla.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatbot.unla.entities.Perfiles;


@Repository("perfilesRepository")
public interface IPerfilesRepository extends JpaRepository<Perfiles, Long>{
	@Query("SELECT p FROM Perfiles p WHERE p.rol = (:rol)")
	public abstract Perfiles getByRol(@Param("rol") String rol);
}

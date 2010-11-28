	DROP TABLE IF EXISTS rg_edge;
	DROP TABLE IF EXISTS rg_vertex;
	DROP TABLE IF EXISTS rg_hwy_lvl;
	
	CREATE TABLE rg_hwy_lvl (
		id INTEGER PRIMARY KEY NOT NULL,
		name VARCHAR(100)
	);
	
	CREATE TABLE rg_vertex (
		id INTEGER NOT NULL,
		osm_node_id BIGINT UNIQUE NOT NULL,  
		lon DOUBLE PRECISION NOT NULL,
		lat DOUBLE PRECISION NOT NULL,
		CONSTRAINT pk_rgv PRIMARY KEY (id)
	);
	
	CREATE TABLE rg_edge( 
		id INTEGER NOT NULL,
		source_id INTEGER NOT NULL,  
		target_id INTEGER NOT NULL, 
		osm_way_id BIGINT NOT NULL,
	 	name VARCHAR NOT NULL, 
	 	ref VARCHAR NOT NULL, 
	 	destination VARCHAR, 
	 	length_meters DOUBLE PRECISION NOT NULL,
		undirected BOOLEAN NOT NULL,
		urban BOOLEAN NOT NULL, 
		roundabout BOOLEAN NOT NULL, 
		hwy_lvl INTEGER NOT NULL, 
		longitudes DOUBLE PRECISION[] NOT NULL, 
		latitudes DOUBLE PRECISION[] NOT NULL,
		CONSTRAINT pk_rge PRIMARY KEY (id),
		CONSTRAINT fk_1 FOREIGN KEY (source_id) REFERENCES rg_vertex (id) INITIALLY DEFERRED DEFERRABLE,
		CONSTRAINT fk_2 FOREIGN KEY (target_id) REFERENCES rg_vertex (id) INITIALLY DEFERRED DEFERRABLE,
		CONSTRAINT fk_3 FOREIGN KEY (hwy_lvl) REFERENCES rg_hwy_lvl (id) INITIALLY DEFERRED DEFERRABLE,
		CONSTRAINT chk_1 CHECK (length_meters >= 0)
	);
	
	
	ALTER TABLE rg_hwy_lvl OWNER TO osm;
	ALTER TABLE rg_vertex OWNER TO osm;
	ALTER TABLE rg_edge OWNER TO osm;
	
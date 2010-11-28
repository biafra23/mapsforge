--
-- PostgreSQL database dump
--

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: metadata; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE metadata (
    import_version integer,
    date bigint,
    maxlat integer,
    minlon integer,
    minlat integer,
    maxlon integer,
    base_zoom_level smallint,
    min_zoom_level smallint,
    max_zoom_level smallint,
    base_zoom_level_low smallint,
    min_zoom_level_low smallint,
    max_zoom_level_low smallint,
    tile_size smallint
);


ALTER TABLE public.metadata OWNER TO osm;

--
-- Name: multipolygons; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE multipolygons (
    outer_way_id bigint NOT NULL,
    inner_way_sequence smallint NOT NULL,
    waynode_sequence smallint,
    latitude integer,
    longitude integer
    
);


ALTER TABLE public.multipolygons OWNER TO osm;

--
-- Name: pois; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE pois (
    id bigint NOT NULL,
    latitude integer,
    longitude integer,
    name_length smallint,
    name text DEFAULT ''::text,
    tags_amount smallint,
    tags text,
    layer smallint,
    elevation smallint,
    housenumber text DEFAULT ''::text    
);


ALTER TABLE public.pois OWNER TO osm;

--
-- Name: pois_tags; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE pois_tags (
    poi_id bigint,
    tag character varying(50)
);


ALTER TABLE public.pois_tags OWNER TO osm;

--
-- Name: pois_to_tiles; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE pois_to_tiles (
    poi_id bigint,
    tile_x integer,
    tile_y integer,
    zoom_level smallint
);


ALTER TABLE public.pois_to_tiles OWNER TO osm;

--
-- Name: pois_to_tiles_low; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE pois_to_tiles_low (
    poi_id bigint,
    tile_x integer,
    tile_y integer,
    zoom_level smallint
);


ALTER TABLE public.pois_to_tiles_low OWNER TO osm;

--
-- Name: waynodes; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE waynodes (
    way_id bigint NOT NULL,
    waynode_sequence smallint,
    latitude integer,
    longitude integer
);


ALTER TABLE public.waynodes OWNER TO osm;

--
-- Name: ways; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE ways (
    id bigint NOT NULL,
    name_length smallint,
    name text DEFAULT ''::text,
    tags_amount smallint,
    tags text,
    layer smallint,
    waynodes_amount integer,
    way_type smallint DEFAULT (1)::smallint,
    convexness smallint,
    label_pos_lat integer,
    label_pos_lon integer,
    inner_way_amount smallint,
    ref text DEFAULT ''::text
);


ALTER TABLE public.ways OWNER TO osm;

--
-- Name: ways_tags; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE ways_tags (
    way_id bigint,
    tag character varying(50)
);


ALTER TABLE public.ways_tags OWNER TO osm;

--
-- Name: ways_to_tiles; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE ways_to_tiles (
    way_id bigint,
    tile_x integer,
    tile_y integer,
    tile_bitmask smallint,
    zoom_level smallint
);


ALTER TABLE public.ways_to_tiles OWNER TO osm;

--
-- Name: ways_to_tiles_low; Type: TABLE; Schema: public; Owner: osm; Tablespace: 
--

CREATE TABLE ways_to_tiles_low (
    way_id bigint,
    tile_x integer,
    tile_y integer,
    tile_bitmask smallint,
    zoom_level smallint
);


ALTER TABLE public.ways_to_tiles_low OWNER TO osm;

--
-- Name: pk_poi_id; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY pois
    ADD CONSTRAINT pk_poi_id PRIMARY KEY (id);


--
-- Name: pk_ways_id; Type: CONSTRAINT; Schema: public; Owner: osm; Tablespace: 
--

ALTER TABLE ONLY ways
    ADD CONSTRAINT pk_ways_id PRIMARY KEY (id);


--
-- Name: multipolygons_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX multipolygons_idx ON multipolygons USING btree (outer_way_id);


--
-- Name: multipolygons_outer_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX multipolygons_outer_idx ON multipolygons USING btree (outer_way_id);


--
-- Name: pois_tags_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX pois_tags_idx ON pois_tags USING btree (tag);


--
-- Name: pois_to_tiles_id_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX pois_to_tiles_id_idx ON pois_to_tiles USING btree (poi_id);


--
-- Name: pois_to_tiles_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX pois_to_tiles_idx ON pois_to_tiles USING btree (tile_x, tile_y);


--
-- Name: waynodes_id_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX waynodes_id_idx ON waynodes USING btree (way_id);


--
-- Name: waynodes_id_sequence_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX waynodes_id_sequence_idx ON waynodes USING btree (way_id, waynode_sequence);


--
-- Name: ways_tags_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX ways_tags_idx ON ways_tags USING btree (tag);


--
-- Name: ways_to_tiles_id_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX ways_to_tiles_id_idx ON ways_to_tiles USING btree (way_id);


--
-- Name: ways_to_tiles_id_tile_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX ways_to_tiles_id_tile_idx ON ways_to_tiles USING btree (way_id, tile_x, tile_y);


--
-- Name: ways_to_tiles_idx; Type: INDEX; Schema: public; Owner: osm; Tablespace: 
--

CREATE INDEX ways_to_tiles_idx ON ways_to_tiles USING btree (tile_x, tile_y);


--
-- Name: fk_multipolygons; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY multipolygons
    ADD CONSTRAINT fk_multipolygons FOREIGN KEY (outer_way_id) REFERENCES ways(id) ON DELETE CASCADE;


--
-- Name: fk_pois_low; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY pois_to_tiles_low
    ADD CONSTRAINT fk_pois_low FOREIGN KEY (poi_id) REFERENCES pois(id) ON DELETE CASCADE;


--
-- Name: fk_pois_tiles; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY pois_to_tiles
    ADD CONSTRAINT fk_pois_tiles FOREIGN KEY (poi_id) REFERENCES pois(id) ON DELETE CASCADE;


--
-- Name: fk_waynodes; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY waynodes
    ADD CONSTRAINT fk_waynodes FOREIGN KEY (way_id) REFERENCES ways(id) ON DELETE CASCADE;


--
-- Name: fk_ways_low; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY ways_to_tiles_low
    ADD CONSTRAINT fk_ways_low FOREIGN KEY (way_id) REFERENCES ways(id) ON DELETE CASCADE;


--
-- Name: fk_waystotiles; Type: FK CONSTRAINT; Schema: public; Owner: osm
--

ALTER TABLE ONLY ways_to_tiles
    ADD CONSTRAINT fk_waystotiles FOREIGN KEY (way_id) REFERENCES ways(id) ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: osm
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM osm;
GRANT ALL ON SCHEMA public TO osm;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

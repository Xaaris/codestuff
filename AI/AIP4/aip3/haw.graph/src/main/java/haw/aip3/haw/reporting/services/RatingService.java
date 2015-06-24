package haw.aip3.haw.reporting.services;

import haw.aip3.haw.graph.rating.nodes.BauteilNode;
import haw.aip3.haw.graph.rating.nodes.GeschaeftspartnerNode;
import haw.aip3.haw.reporting.dto.SalesData;

public interface RatingService {

	Iterable<? extends SalesData> showProductSalesByCity(String string);

	GeschaeftspartnerNode getOrCreateGeschaeftspartner(Long id, String name, String stadt);

	BauteilNode getOrCreateBauteil(Long id, String name);

	void addBestellung(GeschaeftspartnerNode k, BauteilNode p, double preis);

}
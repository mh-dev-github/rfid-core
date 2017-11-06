package com.mh.rfid.enums;

public enum IntegracionType {
	LOCACIONES("LOCACIONES"), 
	PRODUCTOS("PRODUCTOS"), 
	PEDIDOS("PEDIDOS"), 
	SALIDAS_TIENDA("SALIDAS TIENDA"), 
	ORDENES_DE_PRODUCCION("ÓRDENES DE PRODUCCIÓN"), 
	ENTRADAS_PT("ENTRADAS DE PRODUCTO TERMINADO"),
	VENTAS("VENTAS"),
	REMISIONES("REMISIONES"),
	
	ALERTAS_LOGS("ALERTAS LOGS"),
	CONSOLIDADOS_LOGS("CONSOLIDADO LOGS")
	
	;
	
	IntegracionType(String nombre) {
		this.nombrePlural = nombre;
	}

	private final String nombrePlural;

	public String getNombrePlural() {
		return this.nombrePlural;
	};

}

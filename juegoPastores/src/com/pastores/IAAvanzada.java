package com.pastores;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IAAvanzada {

	private static final String GAMEOVER = "GAMEOVER";
	// Tríos de filas que un humano reconoce como "equilibrados" (ordenados)
	private static final List<String> TRIOS = Arrays.asList("1,2,3", "1,4,5", "2,4,6", "3,4,7", "2,5,7", "3,5,6");

	public static void main(String[] args)
	{
		BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
		Random azar = new Random();

		String linea = leerLinea(lector);
		while(linea != null && !GAMEOVER.equals(linea))
		{
			System.out.println(elegirJugada(convertirAEnteros(linea), azar));
			System.out.flush();
			linea = leerLinea(lector);
		}
	}

	/** Lee una línea del árbitro; devuelve null si se cerró la tubería. */
	private static String leerLinea(BufferedReader lector)
	{
		String linea = null;
		try {
			linea = lector.readLine();
		} catch (IOException e) {
			System.err.println("Error al leer del árbitro");
		}
		return linea;
	}

	/** Convierte "3,2,0,4" en {3,2,0,4}. */
	private static int[] convertirAEnteros(String linea)
	{
		String[] partes = linea.split(",");
		int[] enteros = new int[partes.length];

		for(int i = 0; i < partes.length; i++)
		{
			enteros[i] = Integer.parseInt(partes[i].trim());
		}
		return enteros;
	}

	/** Prueba todas las jugadas y elige: ganadora > buena > segura > aleatoria. */
	private static String elegirJugada(int[] filas, Random azar)
	{
		List<String> ganadoras = new ArrayList<>();
		List<String> buenas = new ArrayList<>();
		List<String> seguras = new ArrayList<>();

		for(int i = 0; i < filas.length; i++)
		{
			for(int cantidad = 1; cantidad <= filas[i]; cantidad++)
			{
				clasificarJugada(filas, i, cantidad, ganadoras, buenas, seguras);
			}
		}
		return decidir(filas, azar, ganadoras, buenas, seguras);
	}

	/** Simula la jugada y la guarda en la lista que le corresponde. */
	private static void clasificarJugada(int[] filas, int fila, int cantidad,
			List<String> ganadoras, List<String> buenas, List<String> seguras)
	{
		int[] copia = filas.clone();
		copia[fila] -= cantidad;
		int total = contarPiedras(copia);
		String jugada = fila + "," + cantidad;

		if(total == 1)
		{
			ganadoras.add(jugada);
		}
		else if(total >= 2 && esPosicionBuena(copia))
		{
			buenas.add(jugada);
		}
		else if(total >= 2 && !rivalPuedeDejarUna(copia))
		{
			seguras.add(jugada);
		}
	}

	/** Aplica el orden de preferencia; si no hay nada, juega al azar. */
	private static String decidir(int[] filas, Random azar, List<String> ganadoras,
			List<String> buenas, List<String> seguras)
	{
		String jugada;
		if(!ganadoras.isEmpty())
		{
			jugada = elegirAlAzar(ganadoras, azar);
		}
		else if(!buenas.isEmpty())
		{
			jugada = elegirAlAzar(buenas, azar);
		}
		else if(!seguras.isEmpty())
		{
			jugada = elegirAlAzar(seguras, azar);
		}
		else
		{
			jugada = jugadaAleatoria(filas, azar);
		}
		return jugada;
	}

	private static String elegirAlAzar(List<String> jugadas, Random azar)
	{
		return jugadas.get(azar.nextInt(jugadas.size()));
	}

	/** El rival puede dejar 1 piedra si queda una sola fila con 2 o más, o una fila con 1 más otra. */
	private static boolean rivalPuedeDejarUna(int[] filas)
	{
		int total = contarPiedras(filas);
		boolean puede = false;

		for(int piedrasFila : filas)
		{
			int resto = total - piedrasFila;
			puede = puede || (resto == 0 && piedrasFila >= 2) || (resto == 1 && piedrasFila > 0);
		}
		return puede;
	}

	/** Posición que conviene dejar: gemelas o trío equilibrado, con alguna fila de 2 o más. */
	private static boolean esPosicionBuena(int[] filas)
	{
		int[] ordenada = Arrays.stream(filas).filter(n -> n > 0).sorted().toArray();

		return ordenada[ordenada.length - 1] >= 2 && (sonGemelas(ordenada) || esTrio(ordenada));
	}

	/** Las filas (ordenadas) se emparejan en iguales: {2,2,5,5}. */
	private static boolean sonGemelas(int[] ordenada)
	{
		boolean gemelas = ordenada.length % 2 == 0;

		for(int i = 0; i + 1 < ordenada.length; i += 2)
		{
			gemelas = gemelas && ordenada[i] == ordenada[i + 1];
		}
		return gemelas;
	}

	private static boolean esTrio(int[] ordenada)
	{
		return ordenada.length == 3
				&& TRIOS.contains(ordenada[0] + "," + ordenada[1] + "," + ordenada[2]);
	}

	private static int contarPiedras(int[] filas)
	{
		int total = 0;
		for(int piedrasFila : filas)
		{
			total += piedrasFila;
		}
		return total;
	}

	/** Plan B: fila no vacía al azar y cantidad válida (el elegirJugada de IABasica). */
	private static String jugadaAleatoria(int[] filas, Random azar)
	{
		int candidatas = 0;
		for(int piedrasFila : filas)
		{
			if(piedrasFila > 0)
			{
				candidatas++;
			}
		}

		int k = azar.nextInt(candidatas);
		int vistas = 0;
		int elegida = -1;

		for(int i = 0; i < filas.length; i++)
		{
			if(filas[i] > 0)
			{
				if(vistas == k)
				{
					elegida = i;
				}
				vistas++;
			}
		}
		return elegida + "," + (1 + azar.nextInt(filas[elegida]));
	}
}
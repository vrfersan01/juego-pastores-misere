package com.pastores;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class IABasica {

	private static final String GAMEOVER = "GAMEOVER";
	
	public static void main(String[] args)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Random numero = new Random();
		
		String linea = leerLinea(br);
		while(linea != null && !GAMEOVER.equals(linea))
		{
			int[] filas = convertirAEnteros(linea);
			String jugada = elegirJugada(filas, numero);

			System.out.println(jugada);
			System.out.flush();

			linea = leerLinea(br);
		}
	}
	
	private static String leerLinea(BufferedReader br)
	{
		String linea;
		try {
			linea = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			linea = null;
			e.printStackTrace();
		}
		return linea;
	}
	
	private static int[] convertirAEnteros(String linea)
	{
		String[]caracteres = linea.split(",");
		int[] enteros = new int[caracteres.length];
		for(int i = 0; i < caracteres.length; i++)
		{
			enteros[i]=Integer.parseInt(caracteres[i].trim());
		}
		
		return enteros;
	}
	
	
	private static String elegirJugada(int[] filas, Random numero)
	{
		int candidatas = 0;
		for(int piedrasFila : filas)
		{
			if(piedrasFila > 0)
			{
				candidatas++;
			}
		}

		int k = numero.nextInt(candidatas);
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

		int cantidad = 1 + numero.nextInt(filas[elegida]);

		return elegida + "," + cantidad;
	}

}

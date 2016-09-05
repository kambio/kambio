package emetcode.crypto.bitshake.locale;

public class str_es extends L {
	public static void set() {
		could_not_process_bytes = "No se pudo procesar null bytes.";
		cannot_calc_sha_of_output_data = "No se pudo calcular el sha de los datos de salida.";
		cannot_calc_sha_of_processed_data = "No se pudo calcular el sha de los datos procesados.";
		could_not_encrypt_bytes = "No se pudo encryiptar los bytes";
		could_not_decrypt_bytes = "No se pudo desencriptar los bytes";
		data_verification_failed = "La verificacion de los datos (sha) fallo." +
				"Llave equivocada o archivo corrupto.";
		bytes_seem_raw_bytes = "Procesando archivo con encabezado de sha " + 
				"pero parecen ser bytes crudos (sin formato). Use la opcion apropiada.";
		bytes_seem_with_sha_verif = "Procesando bytes crudos (sin formato)  " + 
				"pero parecen tener un encabezado con sha. Use la opcion apropiada.";
		no_sha_header_found = "Procesando bytes con encabezado de sha  " + 
				"pero no se encontro encabezado. Bytes corruptos.";				
		header_error_no_bytes_found = "Error en encabezado. No hay bytes para procesar. Datos corruptos.";
		internal_err_null_src = "Error interno. null src.";
		internal_err_invalid_idx = "Error interno. invalid idx.";
		internal_err_invalid_idx_2 = "Error interno. invalid idx 2.";
		cannot_set_signer_data = "No se pudo fijar los datos del firmante.";
		cannot_read_signer_data = "No se pudo leer los datos del firmante.";
		cannot_write_signer_data_1 = "No se pudo escribir los datos del firmante 1.";
		given_signer_is_not_equal = "El firmante leido y el dado son diferentes.";
		internal_error_uncomplete_signer_arguments = 
			"Error interno. No su puede firmar. Argumentos de firmante incompletos.";
		invalid_signature_size = "Tamaño invalido de firma.";
		internal_error_cannot_check = "Error interno. No se puede verificar.";
		invalid_length = "longitud invalida";
		invalid_format_no_dots = "Formato invalido. Sin puntos.";
		invalid_format_no_prefix = "Formato invalido. Sin prefijo.";
		invalid_format_no_sufix = "Formato invalido. Sin sufijo.";
		cannot_write_null_data = "No se puede escribir datos nulos.";
		cannot_write_empty_data = "No se puede escribir datos vacios.";
		cannot_encrypt_data = "No se pudo encriptar los datos.";
		cannot_write_data = "No se pudo escribir los datos.";
		cannot_decrypt_data = "No se pudo desencriptsr los datos.";
		null_dir = "null dir";
		not_a_dir = "archivo %s no es un directorio";
		cannot_get_canonical_form_of = "No se pudo obtener el nombre canonico del archivo %s";
		cannot_delete_dir = "No se pudo borrar el directorio %s";
		cannot_create_zip_file = "No se pudo crear el archivo comprimido (zip) %s";
	}
}

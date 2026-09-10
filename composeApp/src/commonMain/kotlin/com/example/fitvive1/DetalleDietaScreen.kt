package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitvive1.database.ApiRepository
import com.example.fitvive1.utils.calcularTDEE
import com.example.fitvive1.utils.distribuirCalorias
import kotlin.math.roundToInt

data class Comida(
    val tipo: String,
    val nombre: String,
    val ingredientes: List<String>,
    val calorias: Int,
    val proteinas: Int,
    val carbohidratos: Int,
    val grasas: Int
)

data class PlanDieta(
    val caloriasObjetivo: Int,
    val caloriasQuema: String,
    val comidas: List<Comida>
)

private fun esVolumen(objetivo: String) = objetivo.contains("volumen", ignoreCase = true)
private fun esDeficit(objetivo: String) = objetivo.contains("ficit", ignoreCase = true)

val NOMBRES_DIA = listOf("Descanso", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

fun obtenerPlanDieta(dia: Int, objetivo: String, caloriasObjetivo: Int): PlanDieta {
    val calQuema = when {
        esVolumen(objetivo) -> "2200–2400 kcal"
        else                -> "2000–2200 kcal"
    }

    val comidas: List<Comida> = when (dia) {
        1 -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Tostadas con huevos revueltos y jamón"
                    esDeficit(objetivo) -> "Yogur griego con frutos rojos"
                    else                -> "Avena con plátano"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("3 rebanadas pan integral", "3 huevos", "80 g jamón york", "1 cdta aceite de oliva")
                    esDeficit(objetivo) -> listOf("200 g yogur griego 0%", "150 g frutos rojos", "1 cdta semillas de chía")
                    else                -> listOf("70 g avena", "1 plátano", "250 ml leche semidesnatada", "Canela al gusto")
                },
                calorias      = when { esVolumen(objetivo) -> 600; esDeficit(objetivo) -> 370; else -> 460 },
                proteinas     = when { esVolumen(objetivo) -> 36;  esDeficit(objetivo) -> 24;  else -> 16  },
                carbohidratos = when { esVolumen(objetivo) -> 55;  esDeficit(objetivo) -> 42;  else -> 78  },
                grasas        = when { esVolumen(objetivo) -> 24;  esDeficit(objetivo) -> 6;   else -> 9   }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Arroz con pollo a la plancha"
                    esDeficit(objetivo) -> "Ensalada de pollo con tomate y huevo"
                    else                -> "Pasta con atún y tomate"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g pechuga de pollo", "200 g arroz", "Pimiento", "Cebolla", "Aceite de oliva")
                    esDeficit(objetivo) -> listOf("180 g pechuga de pollo plancha", "1 huevo duro", "2 tomates", "Lechuga", "Pepino", "Aceite de oliva")
                    else                -> listOf("180 g pasta integral", "1 lata de atún en agua", "2 tomates", "Ajo", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 850; esDeficit(objetivo) -> 530; else -> 680 },
                proteinas     = when { esVolumen(objetivo) -> 68;  esDeficit(objetivo) -> 55;  else -> 44  },
                carbohidratos = when { esVolumen(objetivo) -> 110; esDeficit(objetivo) -> 20;  else -> 88  },
                grasas        = when { esVolumen(objetivo) -> 14;  esDeficit(objetivo) -> 18;  else -> 11  }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Batido casero de plátano y avena"
                    esDeficit(objetivo) -> "Manzana con queso fresco"
                    else                -> "Yogur griego con fruta y miel"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("40 g avena", "1 plátano", "250 ml leche", "1 cdta miel")
                    esDeficit(objetivo) -> listOf("1 manzana", "100 g queso fresco 0%")
                    else                -> listOf("200 g yogur griego", "1 naranja", "1 cdta miel")
                },
                calorias      = when { esVolumen(objetivo) -> 350; esDeficit(objetivo) -> 190; else -> 240 },
                proteinas     = when { esVolumen(objetivo) -> 14;  esDeficit(objetivo) -> 12;  else -> 18  },
                carbohidratos = when { esVolumen(objetivo) -> 58;  esDeficit(objetivo) -> 30;  else -> 28  },
                grasas        = when { esVolumen(objetivo) -> 7;   esDeficit(objetivo) -> 1;   else -> 8   }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Salmón al horno con patatas"
                    esDeficit(objetivo) -> "Tortilla francesa con ensalada"
                    else                -> "Pollo a la plancha con verduras"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g salmón", "250 g patatas", "Perejil", "Ajo", "Aceite de oliva")
                    esDeficit(objetivo) -> listOf("3 huevos", "Lechuga", "Tomate cherry", "Pepino", "Aceite de oliva")
                    else                -> listOf("200 g pechuga de pollo", "Brócoli", "Judías verdes", "Ajo", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 510; else -> 620 },
                proteinas     = when { esVolumen(objetivo) -> 50;  esDeficit(objetivo) -> 24;  else -> 48  },
                carbohidratos = when { esVolumen(objetivo) -> 60;  esDeficit(objetivo) -> 10;  else -> 20  },
                grasas        = when { esVolumen(objetivo) -> 26;  esDeficit(objetivo) -> 35;  else -> 24  }
            )
        )
        2 -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Avena con plátano y miel"
                    esDeficit(objetivo) -> "Tostada integral con tomate y aceite"
                    else                -> "Tortilla francesa con tostadas"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("90 g avena", "1 plátano", "2 cdtas miel", "300 ml leche")
                    esDeficit(objetivo) -> listOf("2 rebanadas pan integral", "1 tomate grande", "Aceite de oliva", "Sal y orégano")
                    else                -> listOf("3 huevos", "2 rebanadas pan integral", "Aceite de oliva", "Sal")
                },
                calorias      = when { esVolumen(objetivo) -> 580; esDeficit(objetivo) -> 360; else -> 450 },
                proteinas     = when { esVolumen(objetivo) -> 22;  esDeficit(objetivo) -> 10;  else -> 22  },
                carbohidratos = when { esVolumen(objetivo) -> 100; esDeficit(objetivo) -> 56;  else -> 42  },
                grasas        = when { esVolumen(objetivo) -> 11;  esDeficit(objetivo) -> 8;   else -> 16  }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Pasta con carne picada"
                    esDeficit(objetivo) -> "Merluza al vapor con verduras y arroz"
                    else                -> "Lentejas"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("200 g pasta", "250 g carne picada", "Tomate natural", "Ajo", "Queso rallado")
                    esDeficit(objetivo) -> listOf("220 g merluza", "Brócoli", "Zanahoria", "80 g arroz integral", "Zumo de limón")
                    else                -> listOf("280 g lentejas cocidas", "Puerro", "Zanahoria", "Cebolla", "Tomate", "Laurel")
                },
                calorias      = when { esVolumen(objetivo) -> 860; esDeficit(objetivo) -> 540; else -> 680 },
                proteinas     = when { esVolumen(objetivo) -> 62;  esDeficit(objetivo) -> 46;  else -> 38  },
                carbohidratos = when { esVolumen(objetivo) -> 105; esDeficit(objetivo) -> 55;  else -> 88  },
                grasas        = when { esVolumen(objetivo) -> 24;  esDeficit(objetivo) -> 9;   else -> 9   }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Mix de frutos secos con plátano"
                    esDeficit(objetivo) -> "Yogur natural con kiwi"
                    else                -> "Tostada con pavo y tomate"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("35 g nueces", "35 g almendras", "1 plátano")
                    esDeficit(objetivo) -> listOf("200 g yogur natural 0%", "2 kiwis")
                    else                -> listOf("2 tostadas integrales", "70 g pechuga de pavo", "1 tomate")
                },
                calorias      = when { esVolumen(objetivo) -> 360; esDeficit(objetivo) -> 160; else -> 250 },
                proteinas     = when { esVolumen(objetivo) -> 11;  esDeficit(objetivo) -> 8;   else -> 18  },
                carbohidratos = when { esVolumen(objetivo) -> 44;  esDeficit(objetivo) -> 24;  else -> 32  },
                grasas        = when { esVolumen(objetivo) -> 22;  esDeficit(objetivo) -> 1;   else -> 4   }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Ternera con arroz"
                    esDeficit(objetivo) -> "Revuelto de espárragos"
                    else                -> "Pavo a la plancha con ensalada"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g filete de ternera", "150 g arroz", "Ensalada verde", "Aceite de oliva")
                    esDeficit(objetivo) -> listOf("3 huevos", "250 g espárragos", "1 cdta aceite de oliva", "Sal")
                    else                -> listOf("220 g filete de pavo", "Lechuga", "Tomate", "Pepino", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 540; else -> 620 },
                proteinas     = when { esVolumen(objetivo) -> 60;  esDeficit(objetivo) -> 24;  else -> 48  },
                carbohidratos = when { esVolumen(objetivo) -> 72;  esDeficit(objetivo) -> 10;  else -> 12  },
                grasas        = when { esVolumen(objetivo) -> 16;  esDeficit(objetivo) -> 30;  else -> 22  }
            )
        )
        3 -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Yogur con granola y plátano"
                    esDeficit(objetivo) -> "Fruta variada con queso fresco"
                    else                -> "Café con tostadas y jamón york"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("200 g yogur natural", "80 g granola", "1 plátano", "2 cdtas miel")
                    esDeficit(objetivo) -> listOf("1 manzana", "1 naranja", "1 kiwi", "150 g queso fresco 0%")
                    else                -> listOf("2 tostadas integrales", "80 g jamón york", "Café con leche", "1 tomate")
                },
                calorias      = when { esVolumen(objetivo) -> 620; esDeficit(objetivo) -> 350; else -> 460 },
                proteinas     = when { esVolumen(objetivo) -> 20;  esDeficit(objetivo) -> 16;  else -> 22  },
                carbohidratos = when { esVolumen(objetivo) -> 100; esDeficit(objetivo) -> 50;  else -> 46  },
                grasas        = when { esVolumen(objetivo) -> 14;  esDeficit(objetivo) -> 2;   else -> 9   }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Lentejas con chorizo"
                    esDeficit(objetivo) -> "Pollo a la plancha con ensalada y arroz"
                    else                -> "Arroz con verduras salteadas"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("300 g lentejas cocidas", "100 g chorizo", "Puerro", "Zanahoria", "Tomate", "Pan de centeno")
                    esDeficit(objetivo) -> listOf("200 g pechuga de pollo", "Lechuga", "Tomate", "100 g arroz integral", "Aceite de oliva")
                    else                -> listOf("180 g arroz", "Pimiento", "Zanahoria", "Brócoli", "Cebolla", "Aceite de sésamo")
                },
                calorias      = when { esVolumen(objetivo) -> 860; esDeficit(objetivo) -> 540; else -> 680 },
                proteinas     = when { esVolumen(objetivo) -> 48;  esDeficit(objetivo) -> 50;  else -> 16  },
                carbohidratos = when { esVolumen(objetivo) -> 108; esDeficit(objetivo) -> 55;  else -> 108 },
                grasas        = when { esVolumen(objetivo) -> 24;  esDeficit(objetivo) -> 12;  else -> 9   }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Tostadas con mantequilla de cacahuete"
                    esDeficit(objetivo) -> "Zanahorias con hummus"
                    else                -> "Fruta y frutos secos"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("2 tostadas integrales", "35 g mantequilla de cacahuete", "1 plátano")
                    esDeficit(objetivo) -> listOf("3 zanahorias", "60 g hummus")
                    else                -> listOf("1 manzana", "1 naranja", "20 g nueces")
                },
                calorias      = when { esVolumen(objetivo) -> 320; esDeficit(objetivo) -> 170; else -> 240 },
                proteinas     = when { esVolumen(objetivo) -> 12;  esDeficit(objetivo) -> 5;   else -> 4   },
                carbohidratos = when { esVolumen(objetivo) -> 48;  esDeficit(objetivo) -> 22;  else -> 38  },
                grasas        = when { esVolumen(objetivo) -> 13;  esDeficit(objetivo) -> 7;   else -> 10  }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Pizza casera integral con pollo"
                    esDeficit(objetivo) -> "Crema de verduras con huevo"
                    else                -> "Tortilla de patatas fit"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("1 base integral", "150 g pollo", "Tomate frito", "Mozzarella", "Champiñones", "Orégano")
                    esDeficit(objetivo) -> listOf("2 huevos", "Calabaza", "Zanahoria", "Cebolla", "Caldo vegetal")
                    else                -> listOf("3 huevos", "250 g patata cocida", "Cebolla", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 540; else -> 620 },
                proteinas     = when { esVolumen(objetivo) -> 48;  esDeficit(objetivo) -> 18;  else -> 22  },
                carbohidratos = when { esVolumen(objetivo) -> 82;  esDeficit(objetivo) -> 30;  else -> 65  },
                grasas        = when { esVolumen(objetivo) -> 20;  esDeficit(objetivo) -> 28;  else -> 22  }
            )
        )
        4 -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Tortitas fit de avena"
                    esDeficit(objetivo) -> "Yogur con semillas de chía y fruta"
                    else                -> "Muesli con leche y fruta"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("100 g avena", "3 huevos", "1 plátano", "Canela", "Levadura", "Miel")
                    esDeficit(objetivo) -> listOf("200 g yogur griego 0%", "2 cdas semillas de chía", "1 kiwi", "1 naranja", "Menta")
                    else                -> listOf("70 g muesli", "250 ml leche", "1 plátano", "Uvas pasas")
                },
                calorias      = when { esVolumen(objetivo) -> 600; esDeficit(objetivo) -> 370; else -> 470 },
                proteinas     = when { esVolumen(objetivo) -> 30;  esDeficit(objetivo) -> 22;  else -> 15  },
                carbohidratos = when { esVolumen(objetivo) -> 85;  esDeficit(objetivo) -> 38;  else -> 82  },
                grasas        = when { esVolumen(objetivo) -> 16;  esDeficit(objetivo) -> 6;   else -> 9   }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Paella de pollo y verduras"
                    esDeficit(objetivo) -> "Garbanzos con espinacas y pollo"
                    else                -> "Ternera con patatas al horno"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("250 g pollo", "200 g arroz", "Pimiento", "Guisantes", "Tomate", "Azafrán", "Aceite")
                    esDeficit(objetivo) -> listOf("150 g pechuga de pollo", "250 g garbanzos cocidos", "200 g espinacas", "Ajo", "Aceite")
                    else                -> listOf("200 g ternera", "250 g patatas", "Romero", "Ajo", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 860; esDeficit(objetivo) -> 530; else -> 680 },
                proteinas     = when { esVolumen(objetivo) -> 62;  esDeficit(objetivo) -> 40;  else -> 48  },
                carbohidratos = when { esVolumen(objetivo) -> 108; esDeficit(objetivo) -> 55;  else -> 60  },
                grasas        = when { esVolumen(objetivo) -> 16;  esDeficit(objetivo) -> 11;  else -> 20  }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Pan con pavo y queso"
                    esDeficit(objetivo) -> "Fruta variada"
                    else                -> "Yogur griego con miel y nueces"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("2 rebanadas pan integral", "100 g pechuga de pavo", "2 lonchas queso")
                    esDeficit(objetivo) -> listOf("1 naranja", "1 pera", "1 kiwi")
                    else                -> listOf("200 g yogur griego", "1 cdta miel", "20 g nueces")
                },
                calorias      = when { esVolumen(objetivo) -> 340; esDeficit(objetivo) -> 150; else -> 250 },
                proteinas     = when { esVolumen(objetivo) -> 30;  esDeficit(objetivo) -> 2;   else -> 18  },
                carbohidratos = when { esVolumen(objetivo) -> 40;  esDeficit(objetivo) -> 35;  else -> 18  },
                grasas        = when { esVolumen(objetivo) -> 10;  esDeficit(objetivo) -> 0;   else -> 12  }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Ternera con puré de patatas"
                    esDeficit(objetivo) -> "Pollo a la plancha con brócoli"
                    else                -> "Revuelto de huevos con verduras"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g ternera", "300 g patata", "Mantequilla", "Leche", "Ensalada verde")
                    esDeficit(objetivo) -> listOf("200 g pechuga de pollo", "350 g brócoli", "Ajo", "Zumo de limón", "Aceite")
                    else                -> listOf("3 huevos", "Pimiento rojo y verde", "Champiñones", "Cebolla", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 550; else -> 600 },
                proteinas     = when { esVolumen(objetivo) -> 55;  esDeficit(objetivo) -> 52;  else -> 26  },
                carbohidratos = when { esVolumen(objetivo) -> 70;  esDeficit(objetivo) -> 16;  else -> 14  },
                grasas        = when { esVolumen(objetivo) -> 22;  esDeficit(objetivo) -> 14;  else -> 38  }
            )
        )
        5 -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Huevos revueltos con jamón y tostadas"
                    esDeficit(objetivo) -> "Tostada de centeno con tomate"
                    else                -> "Yogur natural con granola y fruta"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("4 huevos", "100 g jamón york", "3 tostadas integrales", "1 cdta mantequilla")
                    esDeficit(objetivo) -> listOf("2 rebanadas pan de centeno", "1 tomate grande", "Aceite de oliva", "Orégano")
                    else                -> listOf("200 g yogur natural", "50 g granola", "100 g fresas", "1 cdta miel")
                },
                calorias      = when { esVolumen(objetivo) -> 610; esDeficit(objetivo) -> 360; else -> 450 },
                proteinas     = when { esVolumen(objetivo) -> 44;  esDeficit(objetivo) -> 10;  else -> 14  },
                carbohidratos = when { esVolumen(objetivo) -> 52;  esDeficit(objetivo) -> 52;  else -> 70  },
                grasas        = when { esVolumen(objetivo) -> 26;  esDeficit(objetivo) -> 7;   else -> 9   }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Cocido madrileño"
                    esDeficit(objetivo) -> "Menestra de verduras con pavo"
                    else                -> "Arroz con gambas"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("250 g garbanzos", "120 g ternera", "Verduras", "Fideos", "Chorizo", "Morcillo")
                    esDeficit(objetivo) -> listOf("220 g pechuga de pavo", "Judías verdes", "Zanahoria", "Guisantes", "Caldo de verduras")
                    else                -> listOf("180 g arroz", "250 g gambas", "Ajo", "Perejil", "Aceite de oliva", "Pimiento")
                },
                calorias      = when { esVolumen(objetivo) -> 870; esDeficit(objetivo) -> 540; else -> 690 },
                proteinas     = when { esVolumen(objetivo) -> 55;  esDeficit(objetivo) -> 50;  else -> 40  },
                carbohidratos = when { esVolumen(objetivo) -> 90;  esDeficit(objetivo) -> 30;  else -> 88  },
                grasas        = when { esVolumen(objetivo) -> 28;  esDeficit(objetivo) -> 9;   else -> 12  }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Batido de avena y frutos rojos"
                    esDeficit(objetivo) -> "Pepino con queso fresco"
                    else                -> "Tostada integral con aguacate"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("50 g avena", "250 ml leche", "200 g frutos rojos", "2 cdtas miel")
                    esDeficit(objetivo) -> listOf("1 pepino grande", "120 g queso fresco 0%", "Sal y pimienta")
                    else                -> listOf("2 tostadas integrales", "1/2 aguacate", "1 tomate", "Zumo de limón")
                },
                calorias      = when { esVolumen(objetivo) -> 320; esDeficit(objetivo) -> 140; else -> 260 },
                proteinas     = when { esVolumen(objetivo) -> 14;  esDeficit(objetivo) -> 12;  else -> 6   },
                carbohidratos = when { esVolumen(objetivo) -> 52;  esDeficit(objetivo) -> 10;  else -> 32  },
                grasas        = when { esVolumen(objetivo) -> 7;   esDeficit(objetivo) -> 2;   else -> 11  }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Lubina al horno con patatas"
                    esDeficit(objetivo) -> "Ensalada completa con atún"
                    else                -> "Revuelto de setas con pavo"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g lubina", "250 g patatas", "Perejil", "Ajo", "Zumo de limón", "Aceite")
                    esDeficit(objetivo) -> listOf("Lechuga", "Tomate", "Pepino", "2 latas de atún al natural", "Aceitunas", "Aceite")
                    else                -> listOf("3 huevos", "200 g setas", "120 g pavo en dados", "Ajo", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 560; else -> 600 },
                proteinas     = when { esVolumen(objetivo) -> 50;  esDeficit(objetivo) -> 42;  else -> 42  },
                carbohidratos = when { esVolumen(objetivo) -> 60;  esDeficit(objetivo) -> 14;  else -> 10  },
                grasas        = when { esVolumen(objetivo) -> 22;  esDeficit(objetivo) -> 22;  else -> 30  }
            )
        )
        else -> listOf(
            Comida(
                tipo = "DESAYUNO",
                nombre = when {
                    esVolumen(objetivo) -> "Bol de avena con frutos secos"
                    esDeficit(objetivo) -> "Café con leche y tostada con jamón"
                    else                -> "Tostadas con aguacate y tomate"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("90 g avena", "250 ml leche", "35 g nueces", "35 g almendras", "1 plátano")
                    esDeficit(objetivo) -> listOf("Café con leche desnatada", "2 tostadas integrales", "60 g jamón york", "1 tomate")
                    else                -> listOf("2 tostadas integrales", "1/2 aguacate", "1 tomate", "Aceite de oliva", "Sal")
                },
                calorias      = when { esVolumen(objetivo) -> 620; esDeficit(objetivo) -> 360; else -> 460 },
                proteinas     = when { esVolumen(objetivo) -> 24;  esDeficit(objetivo) -> 18;  else -> 10  },
                carbohidratos = when { esVolumen(objetivo) -> 92;  esDeficit(objetivo) -> 40;  else -> 46  },
                grasas        = when { esVolumen(objetivo) -> 26;  esDeficit(objetivo) -> 7;   else -> 18  }
            ),
            Comida(
                tipo = "COMIDA",
                nombre = when {
                    esVolumen(objetivo) -> "Hamburguesa casera con patatas al horno"
                    esDeficit(objetivo) -> "Pollo al horno con verduras"
                    else                -> "Garbanzos con espinacas y chorizo"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("250 g carne picada", "Pan integral", "Lechuga", "Tomate", "250 g patatas al horno")
                    esDeficit(objetivo) -> listOf("220 g pechuga de pollo", "Pimiento", "Calabacín", "Cebolla", "Ajo", "Aceite")
                    else                -> listOf("250 g garbanzos", "200 g espinacas", "70 g chorizo", "Ajo", "Tomate frito")
                },
                calorias      = when { esVolumen(objetivo) -> 870; esDeficit(objetivo) -> 540; else -> 680 },
                proteinas     = when { esVolumen(objetivo) -> 58;  esDeficit(objetivo) -> 50;  else -> 36  },
                carbohidratos = when { esVolumen(objetivo) -> 82;  esDeficit(objetivo) -> 22;  else -> 72  },
                grasas        = when { esVolumen(objetivo) -> 30;  esDeficit(objetivo) -> 12;  else -> 20  }
            ),
            Comida(
                tipo = "MERIENDA",
                nombre = when {
                    esVolumen(objetivo) -> "Batido proteico casero"
                    esDeficit(objetivo) -> "Yogur 0% con fresas"
                    else                -> "Naranja y frutos secos"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("40 g avena", "1 plátano", "200 ml leche", "1 cda mantequilla de cacahuete")
                    esDeficit(objetivo) -> listOf("200 g yogur natural 0%", "150 g fresas")
                    else                -> listOf("2 naranjas", "25 g nueces")
                },
                calorias      = when { esVolumen(objetivo) -> 310; esDeficit(objetivo) -> 170; else -> 240 },
                proteinas     = when { esVolumen(objetivo) -> 16;  esDeficit(objetivo) -> 12;  else -> 5   },
                carbohidratos = when { esVolumen(objetivo) -> 48;  esDeficit(objetivo) -> 24;  else -> 38  },
                grasas        = when { esVolumen(objetivo) -> 10;  esDeficit(objetivo) -> 0;   else -> 10  }
            ),
            Comida(
                tipo = "CENA",
                nombre = when {
                    esVolumen(objetivo) -> "Solomillo a la plancha con arroz"
                    esDeficit(objetivo) -> "Dorada al vapor con verduras"
                    else                -> "Pollo al horno con ensalada"
                },
                ingredientes = when {
                    esVolumen(objetivo) -> listOf("220 g solomillo de ternera", "150 g arroz", "Ensalada verde", "Aceite de oliva")
                    esDeficit(objetivo) -> listOf("220 g dorada", "Brócoli", "Zanahoria", "Judías verdes", "Zumo de limón")
                    else                -> listOf("200 g muslo de pollo", "Lechuga", "Tomate", "Pepino", "Aceite de oliva")
                },
                calorias      = when { esVolumen(objetivo) -> 700; esDeficit(objetivo) -> 530; else -> 620 },
                proteinas     = when { esVolumen(objetivo) -> 64;  esDeficit(objetivo) -> 44;  else -> 44  },
                carbohidratos = when { esVolumen(objetivo) -> 72;  esDeficit(objetivo) -> 20;  else -> 18  },
                grasas        = when { esVolumen(objetivo) -> 20;  esDeficit(objetivo) -> 10;  else -> 26  }
            )
        )
    }

    val distribucion = distribuirCalorias(caloriasObjetivo)
    val comidasAjustadas = comidas.map { comida ->
        val caloriasAsignadas = distribucion.paraTipo(comida.tipo)
        val factor = if (comida.calorias > 0) {
            caloriasAsignadas.toDouble() / comida.calorias
        } else {
            1.0
        }
        comida.copy(
            calorias = caloriasAsignadas,
            proteinas = (comida.proteinas * factor).roundToInt(),
            carbohidratos = (comida.carbohidratos * factor).roundToInt(),
            grasas = (comida.grasas * factor).roundToInt()
        )
    }

    return PlanDieta(
        caloriasObjetivo = caloriasObjetivo,
        caloriasQuema = calQuema,
        comidas = comidasAjustadas
    )
}

@Composable
fun DetalleDietaScreen(
    dia: Int,
    objetivo: String,
    pesoUsuario: String,
    alturaUsuario: String,
    sexoUsuario: String,
    apiRepository: ApiRepository,
    comidasCompletadas: Set<String> = emptySet(),
    onComidaCompletadaChange: (tipo: String, completada: Boolean) -> Unit = { _, _ -> },
    onFinalizarClick: () -> Unit
) {
    if (dia == 0) {
        DomingoLibreScreen(onFinalizarClick)
        return
    }

    val tdee = remember(pesoUsuario, alturaUsuario, sexoUsuario, objetivo) {
        calcularTDEE(
            peso = pesoUsuario,
            altura = alturaUsuario,
            sexo = sexoUsuario,
            objetivo = objetivo
        )
    }
    val plan = remember(dia, objetivo, tdee) {
        obtenerPlanDieta(dia, objetivo, tdee)
    }
    var comidasMostradas by remember(
        dia,
        objetivo,
        tdee
    ) {
        mutableStateOf(plan.comidas)
    }

    LaunchedEffect(dia, objetivo, tdee) {
        comidasMostradas = plan.comidas

        comidasMostradas = plan.comidas.map { comidaLocal ->
            val comidaApi = apiRepository.getComida(
                objetivo = objetivo,
                tipo = comidaLocal.tipo,
                caloriasObjetivo = comidaLocal.calorias
            )

            if (comidaApi == null) {
                comidaLocal
            } else {
                comidaLocal.copy(
                    nombre = comidaApi.nombre,
                    calorias = comidaApi.calorias,
                    proteinas = comidaApi.proteinas,
                    carbohidratos = comidaApi.carbohidratos
                )
            }
        }
    }

    val titulo = NOMBRES_DIA.getOrElse(dia) { "Día $dia" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        Text(
            text = titulo,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TarjetaOscura)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(text = "Consumir", fontSize = 11.sp, color = TextoSecundario)
                    Text(
                        text = "${plan.caloriasObjetivo} kcal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Naranja
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TarjetaOscura)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(text = "Quema aprox.", fontSize = 11.sp, color = TextoSecundario)
                    Text(
                        text = plan.caloriasQuema,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        comidasMostradas.forEach { comida ->
            ComidaItem(
                comida = comida,
                completado = comida.tipo in comidasCompletadas,
                onCompletadoChange = { completado ->
                    onComidaCompletadaChange(comida.tipo, completado)
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onFinalizarClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Naranja,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = "FINALIZAR DÍA",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun DomingoLibreScreen(onVolver: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))

        Text(
            text = "Domingo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Día de descanso",
            fontSize = 15.sp,
            color = TextoSecundario
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌿", fontSize = 52.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Descansa y disfruta",
                    fontSize = 14.sp,
                    color = TextoSecundario
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TarjetaOscura)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Hoy no hay plan estricto",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Come lo que te apetezca con moderación. Un día libre es parte de un estilo de vida saludable y sostenible.",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TarjetaOscura)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "RECOMENDACIONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoSecundario,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(14.dp))

                val tips = listOf(
                    Pair("💧", "Mantente bien hidratado durante el día"),
                    Pair("🍽️", "Come despacio y sin distracciones"),
                    Pair("🎉", "Disfruta sin culpa, ¡te lo has ganado!"),
                    Pair("⚠️", "Evita excederte en ultraprocesados")
                )

                tips.forEach { (emoji, texto) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                        Text(emoji, fontSize = 16.sp)
                        Text(
                            text = texto,
                            fontSize = 13.sp,
                            color = TextoSecundario,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onVolver,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Naranja,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = "VOLVER",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun ComidaItem(
    comida: Comida,
    completado: Boolean = false,
    onCompletadoChange: (Boolean) -> Unit = {}
) {
    val emojiTipo = when (comida.tipo) {
        "DESAYUNO" -> "☀️"
        "COMIDA"   -> "🍽️"
        "MERIENDA" -> "🍎"
        else       -> "🌙"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(emojiTipo, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A0D00))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = comida.tipo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Naranja,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            if (completado) {
                Text("✓", fontSize = 18.sp, color = Color(0xFF4CAF50))
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = comida.nombre,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF2A2A2A))
        )

        Spacer(Modifier.height(10.dp))

        comida.ingredientes.forEach { ingrediente ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Naranja)
                )
                Text(
                    text = ingrediente,
                    fontSize = 13.sp,
                    color = TextoSecundario
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "${comida.calorias} kcal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Naranja
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroChip("${comida.proteinas}g prot.")
            MacroChip("${comida.carbohidratos}g carb.")
            MacroChip("${comida.grasas}g grasa")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { onCompletadoChange(!completado) },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (completado) Color(0xFF1A3320) else SuperficieOscura,
                contentColor = if (completado) Color(0xFF4CAF50) else TextoSecundario
            )
        ) {
            Text(
                text = if (completado) "COMPLETADO ✓" else "MARCAR COMPLETADO",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MacroChip(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SuperficieOscura)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            fontSize = 11.sp,
            color = TextoSecundario
        )
    }
}

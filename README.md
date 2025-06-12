# Aplikacja do obróbki obrazów w języku Java

## Cel projektu

Celem zadania było stworzenie aplikacji desktopowej w języku Java, umożliwiającej użytkownikowi obróbkę obrazów graficznych. Projekt został zrealizowany na podstawie user-story i spełnia określone wymagania funkcjonalne.

## Funkcjonalności

- Interfejs graficzny aplikacji został zaprojektowany zgodnie z opisem zawartym w user-story.
  ![image](https://github.com/user-attachments/assets/bcba9905-65a0-474b-8983-a016c565cb89)

- Implementacja funkcji filtrujących wykorzystuje przetwarzanie równoległe – filtry są aplikowane przy użyciu 4 wątków jednocześnie, co znacznie przyspiesza operacje na obrazach.
- Aplikacja umożliwia również:
  - Skalowanie obrazów
  - Obracanie obrazów
  ![image](https://github.com/user-attachments/assets/5bbb2acd-b93e-416a-9eaf-028ceb9d7475)

- Dodatkowo, każda operacja wykonywana w aplikacji jest zapisywana do pliku logu, co pozwala na późniejsze śledzenie działań użytkownika.
![image](https://github.com/user-attachments/assets/9b80de10-dfd2-4601-bcc8-c21215b0cc50)

## Technologie

- Java (Swing lub JavaFX – w zależności od implementacji)
- Programowanie współbieżne (wątki)

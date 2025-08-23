import numpy as np
from sortedcontainers import SortedSet
import heapq


class Point:
    def __init__(self,x,y,label,section=None):
        self.x = x #współrzędna x-owa
        self.y = y #współrzędna y-owa
        self.label = label #etykieta punktu
        self.attach_to = section #przynależności do odcinka (odcinków)
        
    def __eq__(self,other): #przeciążenie operatora (==)
        if self.label=='przeciecie' or other.label=='przeciecie': return self.x==other.x and self.y==other.y
        # kiedy inicjalizuje Q chce mieć w niej wszystkie punkty
        else: return self.x==other.x and self.y==other.y and self.attach_to==other.attach_to
        
    def __gt__(self,other): #przeciążenie operatora (>)
        if self.x==other.x: return self.label>other.label
        return self.x>other.x
    
    def __hash__(self):
        return hash((self.x,self.y))
    
    def __str__(self):
        return f"Point(x={self.x}, y={self.y}, {self.label})"
    
    # wskaźnik do prostej, którą przechowuje
    def set_attach_to(self,section):
        self.attach_to = section
    
class Section:
    def __init__(self,begin, end, index_in_section=None):
        self.begin = begin #lewy koniec odcinka
        self.end = end #prawy koniec odcinka
        self.a = (self.begin.y-self.end.y)/(self.begin.x-self.end.x) #współczynnik nachylenia
        self.b = self.begin.y - self.a * self.begin.x #wyraz wolny
        self.index_in_section = index_in_section


    def update_x(x): #metoda statyczna (pole wspólne dla klasy)
        Section.x = x
        
    def __eq__(self,other):
        return (self.begin == other.begin and self.end == other.end)
    
    def __gt__(self,other):
        return self.a * Section.x + self.b > other.a * Section.x + other.b # jawnie obliczam y dla każdego punktu
    
    def __hash__(self):
        return hash((self.begin, self.end))
    
    def __str__(self):
        return f"begin: {self.begin}, end:{self.end})"



def intersect_point(sect1,sect2):
    # wyznaczam punkt przecięcia obu prostych
    #pobieram dane
    if sect1.a == sect2.a: return None
    a1 = sect1.a
    b1 = sect1.b
    a2 = sect2.a
    b2 = sect2.b
    # jeżeli są równoległe
    if a1==a2: return
    x = (b1-b2)/(a2-a1)
    # sprawdzam, czy punkt jest w obrębie prostych
    if sect1.begin.x <= x <=sect1.end.x and sect2.begin.x<= x <=sect2.end.x:
        return (x,sect1.a*x+sect1.b)


def find_intersections(sections):
    eps=10**(-8)
    def check_intersection_and_add_new_point_to_Q(line1,line2):
        # funkcja sprawdzająca przecięcie otrzymanych odcinków i dodające je
        # do struktury Q

        index_of_both_lines = (min(line1.index_in_section,line2.index_in_section)+1,max(line1.index_in_section,line2.index_in_section)+1)
        if index_of_both_lines not in already_checked:
            already_checked.add(index_of_both_lines)
            new_point = intersect_point(line1,line2)

            if new_point is not None and new_point[0]>actual_point.x: 
                # dodaje punkt do kolejki na miotle
                point_to_add = Point(new_point[0],new_point[1],'przeciecie',(line1,line2))
                # żeby cofnąć się do bazowej wartości
                heapq.heappush(Q,point_to_add)
                # korekcja ma na celu rozwiązać problem z przecięciami, które są równocześnie początkami odcinka
                # dodaje do tablicy wynikowej
                tab_przeciec.append((new_point[0],new_point[1]))

    # zbiór zbadanych już odcinków, dzięki temu nie rozpatruje pare razy tego samego punktu
    already_checked=set()
    # zbiór wynikowy
    tab_przeciec=[]
    # heapq jako struktura Q
    Q = []

    # inicjalizuje strukture punktów
    for index_in_section, (begin_point, end_point) in list(enumerate(sections)):
        print(index_in_section)
        section_of_this_points = Section( Point(begin_point[0],begin_point[1],'begin') , Point(end_point[0],end_point[1],'end'), index_in_section)
        Q.append(Point(begin_point[0],begin_point[1],'begin',section_of_this_points))
        Q.append(Point(end_point[0],end_point[1],'end',section_of_this_points))
    # utworzenie stosu
    heapq.heapify(Q)
    # inicjalizuje strukture stanu
    T = SortedSet()
    
    while len(Q)>0:
        actual_point = heapq.heappop(Q)
        actual_line = actual_point.attach_to
        # rozpatruje przypadek początku odcinka
        if actual_point.label == "begin":
            Section.update_x(actual_point.x)
            T.add(actual_line)
            index = T.index(actual_line)
            if index>0:
                check_intersection_and_add_new_point_to_Q(actual_line,T[index-1])

            if index < len(T)-1:
                check_intersection_and_add_new_point_to_Q(actual_line,T[index+1])

        # rozpatruje przypadek końca odcinka
        elif actual_point.label == "end":
            Section.update_x(actual_point.x)
            index = T.index(actual_line)
            if 0<index<len(T)-1:
                check_intersection_and_add_new_point_to_Q(T[index-1],T[index+1])
            T.discard(actual_line)
            
        # rozpatruje przypadek przecięcia odcinków
        else:
            # pobieram odcniki, które się przecinają w tym punkcie
            actual_line1, actual_line2 = actual_point.attach_to
            # przesuwam miotłe minimalnie przed aktualny punkt
            Section.update_x(actual_point.x-eps)
            # usuwam te odcinki
            T.discard(actual_line1)
            T.discard(actual_line2)
            # przesuwam miotłe minimalnie za aktualny punkt
            Section.update_x(actual_point.x+eps)
            # dodaje odcinki
            T.add(actual_line1)
            T.add(actual_line2)

            # wybieram, aby actual_line1 była wyżej niż actual_line2
            if actual_line1>actual_line2:
                index1 = T.index(actual_line1)
                index2 = T.index(actual_line2)
            else:
                index2 = T.index(actual_line1)
                index1 = T.index(actual_line2)
            
            # sprawdzam przecinanie się nowododanych lini
            if index1 < len(T)-1:
                check_intersection_and_add_new_point_to_Q(T[index1],T[index1+1])
            
            if index2>0:
                check_intersection_and_add_new_point_to_Q(T[index2],T[index2-1])

    
    return tab_przeciec
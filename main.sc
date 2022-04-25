using import itertools
using import Array
using import String
using import Option
# radgeRayden contributed this quick conversion between char and String
inline char->String (c)
    local str : String
    'append str c
    str

# Thank you to radgeRayden for this simple map function
inline map-Array (arr f)
    local new-array : (Array ((typeof arr) . ElementType))
    for el in arr
        'append new-array (f el)
    new-array

fn munch (chr word)
    # Finds the first chr in word and returns the rest of word.
    for i c in (enumerate word)
        if (c == chr)
            let res = (rslice word (i + 1))
            return ((Option String) res)

    return ((Option String) none)
            

fn match (path word)
    # Takes an input path (converting it to a String) and a word to match.
    # Then, for each char in word:
    # It finds the first instance of that char.
    # It slices the input path at that char and returns everything to the right of it, storing the result of that operation.
    # If it does this successfully for every char in word: return true.
    # Otherwise, return false.
    let empty = (String "")

    local pstring = 
        fold (pstring = empty) for c in path
            (pstring .. (char->String c))
    
    for c in word
        let munched = (munch c pstring)
        try 
            pstring = ('unwrap munched)
        else
            return false
    
    # If we reach this point, we've successfully matched the entire String.
    return true

fn get-row (chr)
    # Looks for a chr in a keyboard.
    local keyboard = (arrayof String "qwertyuiop" "asdfghjkl" "zxcvbnm")
    for row keys in (enumerate keyboard)
        for c in keys
            if (chr == (char->String c))
                return ((Option usize) row)
    
    return ((Option usize) none)

fn compress (seq)
    # Takes a sequence of values and removes duplicates.
    local new-seq : (typeof seq)
    for chr in seq 
        if ((countof new-seq) < 1)
            ('append new-seq (copy chr))
        else
            let new-chr = ('last new-seq)
            if (new-chr != chr)
                ('append new-seq (copy chr))
    
    return new-seq

fn get-min-len (seq)
    # Takes a sequence of chars, convert it into a sequence of row numbers,
    # then determine the minimum word length from that.
    local rows : (Array (Option usize))
    for el in seq
        'append rows (get-row el)
    local dst : (Array usize)
    let rowseq = 
        ->> rows
            filter
                (x) -> (imply x bool)
            map
                (el) -> ('force-unwrap el)
            dst
    let result = ((countof (compress rowseq)) - 3)
    return (copy result)

fn get-suggestion (path wordlist)
    # Takes an input path, then:

    local first : (Array String)
    # Selects all words from the Wordlist that start and end with the same letters as the path.
    ->> wordlist
        map copy
        filter
            inline (x)
                (dump (typeof (x @ 0)) (typeof (path @ 0)))
                and
                    (char->String (x @ 0)) == (path @ 0)
                    (char->String ('last x)) == ('last path)
        view first

    # Narrows it down to all words that can be matched out of the path.
    local second : (Array String)
    ->> first
        map copy
        filter
            inline (x)
                (match path x)
        view second
    # Narrows it down further to all words that are longer than the minimum length.
    local third : (Array String)
    ->> second
        map copy
        filter
            inline (x)
                ((countof x) >= (get-min-len path))
        view third
    # Returns the selected words.

    return third



# Testing.
# match:
let p = ((Array String) "a" "s" "d" "f")
(print "False: " (match p (String "word")))
(print "True: " (match p (String "asdf")))

# get-row:
let q = (get-row (String "q"))
let q = 
    try ('unwrap q)
    else
        error "unwrap failed"
print "q: " q

# compress:
let sequence = ((Array String) "q" "q" "q" "w" "w" "e")
let s1 = (compress sequence)
print "compressed " sequence " is " s1

# get-min-len
let lenseq = ((Array String) "P" "u" "m" "p" "e" "r" "n" "i" "c" "k" "e" "l")
print "Min length of sequence 'Pumpernickel': " (get-min-len lenseq)

# get-suggestion:
let wl = ((Array String) "word" "ward" "wrd")
print "Suggestions" 
for w in (get-suggestion ((Array String) "w" "a" "o" "e" "e" "r" "d" "d" "d") wl)
    print w